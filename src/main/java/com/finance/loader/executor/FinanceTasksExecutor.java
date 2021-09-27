
package com.finance.loader.executor;

import com.finance.loader.dto.Notification;
import com.finance.loader.exceptions.TooManyRequestsException;
import com.finance.loader.model.Institution;
import com.finance.loader.model.StockInfoShortened;
import com.finance.loader.props.IEXPropsHolder;
import com.finance.loader.repo.InstitutionRepository;
import com.finance.loader.tasks.NotificationPublisher;
import com.finance.loader.webclient.FinanceLoaderWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FinanceTasksExecutor {

    private final Logger logger = LoggerFactory.getLogger(FinanceTasksExecutor.class.getName());
    private final FinanceLoaderWebClient webClient;
    private final IEXPropsHolder propsHolder;
    private final InstitutionRepository institutionRepository;
    private final NotificationPublisher publisher;

    public FinanceTasksExecutor(FinanceLoaderWebClient webClient, IEXPropsHolder propsHolder, InstitutionRepository institutionRepository, NotificationPublisher publisher) {
        this.webClient = webClient;
        this.propsHolder = propsHolder;
        this.institutionRepository = institutionRepository;
        this.publisher = publisher;
    }

    public void start() {
        logger.info("Institution's information processing...");
        List<Institution> institutions = getGlobalFinancesInfo();
        institutions = institutions.subList(0, 50);
        institutionRepository.saveAll(fetchCompaniesStockInfo(institutions))
                .doOnComplete(this::start)
                .map(institution -> new Notification(
                        Notification.NoticeStatus.COMPLETED,
                        Notification.NoticeType.INFO,
                        LocalDate.now(),
                        LocalDate.now()))
                .collectList().subscribe(publisher::sendMany);
        displayTopOrganizations();
    }

    private List<Institution> getGlobalFinancesInfo() {
        logger.debug("REST request to get all institutions.");
        WebClient.ResponseSpec responseSpec = webClient.getPreparedClient().get().uri(uriBuilder -> uriBuilder
                        .pathSegment("/stable/ref-data/symbols")
                        .queryParam("token", propsHolder.getApiToken()).build())
                .retrieve();
        List<Institution> institutions = responseSpec.bodyToFlux(Institution.class).collectList().block();
        if (institutions != null) {
            logger.info(String.format("Institutions received %d.", institutions.size()));
            return institutions;
        }
        return Collections.emptyList();
    }

    public WebClient.RequestHeadersSpec<?> prepareReceiveCompanyInfoReq(String stock) {
        logger.debug(String.format("Prepare REST request to receive single institution info by stock %s", stock));
        return webClient.getPreparedClient().get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("/stable/stock/".concat(stock.concat("/quote")))
                        .queryParam("token", propsHolder.getApiToken())
                        .build());
    }

    private Mono<Institution> downloadStockInfo(Institution institution) {
        return
                prepareReceiveCompanyInfoReq(institution.getSymbol())
                        .exchangeToMono(clientResponse -> {
                            if (clientResponse.statusCode().is4xxClientError())
                                logger.debug("Stock loading failed. The reason is " + clientResponse.statusCode().getReasonPhrase() + ". " + institution.getSymbol());
                            Mono<StockInfoShortened> res = Mono.empty();
                            if (clientResponse.headers().contentType().isPresent() && MediaType.APPLICATION_JSON_VALUE.contains(clientResponse.headers().contentType().get().getSubtype()))
                                res = clientResponse.bodyToMono(StockInfoShortened.class);
                            return res;
                        })
                        .retryWhen(Retry.backoff(5, Duration.of(1000, ChronoUnit.MILLIS))
                                .filter(error -> error instanceof WebClientResponseException.TooManyRequests || error instanceof WebClientRequestException)
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    publisher.send(new Notification(
                                            Notification.NoticeStatus.NEW,
                                            Notification.NoticeType.ERROR,
                                            LocalDate.now(),
                                            LocalDate.now()));
                                    return new TooManyRequestsException(retrySignal.failure().getMessage());
                                }))
                        .map(stockInfoShortened -> {
                            Optional.ofNullable(institution.getStockHistory()).ifPresentOrElse(
                                    (history) -> history.add(stockInfoShortened),
                                    () -> institution.setStockHistory(Collections.singletonList(stockInfoShortened)));
                            return institution;
                        });
    }

    public List<Institution> highestStockCompanies() {
        return Optional.of(institutionRepository.findAll().toStream()
                .sorted((o1, o2) -> {
                    if (o1.getLastStockInfo().getVolume() != null && o2.getLastStockInfo().getVolume() != null)
                        return o1.getLastStockInfo().getVolume() < o2.getLastStockInfo().getVolume() ? 1 : 0;
                    return 1;
                })
                .collect(Collectors.toList()).subList(0, 5)).orElseThrow(() -> {
            logger.error("Empty list for print provided");
            throw new NullPointerException("Empty list provided");
        });
    }

    public List<Institution> highestChangePercentCompanies() {
        return Optional.of(institutionRepository.findAll().toStream()
                .sorted((o1, o2) -> {
                    if (o1.getLastStockInfo().getChangePercent() != null && o2.getLastStockInfo().getChangePercent() != null)
                        return o1.getLastStockInfo().getChangePercent() < o2.getLastStockInfo().getChangePercent() ? 1 : 0;
                    return 1;
                })
                .collect(Collectors.toList()).subList(0, 5)).orElseThrow(() -> {
            logger.error("Empty list for print provided");
            throw new NullPointerException("Empty list provided");
        });
    }

    public Flux<Institution> fetchCompaniesStockInfo(List<Institution> institutions) {
        return Flux.fromIterable(institutions.stream().filter(Institution::isEnabled).collect(Collectors.toList()))
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(this::downloadStockInfo)
                .ordered((o1, o2) -> (o1.getLastStockInfo().getVolume() != null ? o1.getLastStockInfo().getVolume() : 0)
                        > (o2.getLastStockInfo().getVolume() != null ? o2.getLastStockInfo().getVolume() : -1) ? 1 : 0);
    }

    public void displayTopOrganizations() {
        logger.info(String.format("The highest stock: %s \n ||||||||||||||| \n The gr-st change percent %s2: ",
                highestStockCompanies(),
                highestChangePercentCompanies()));
    }
}
