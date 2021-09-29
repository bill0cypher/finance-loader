
package com.finance.loader.executor;

import com.finance.loader.component.AppProperties;
import com.finance.loader.dto.NotificationDTO;
import com.finance.loader.exceptions.TooManyRequestsException;
import com.finance.loader.model.Institution;
import com.finance.loader.model.StockInfoShortened;
import com.finance.loader.repo.InstitutionRepository;
import com.finance.loader.tasks.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class FinanceTasksExecutor {
    private final WebClient webClient;
    private final AppProperties appProperties;
    private final InstitutionRepository institutionRepository;
    private final NotificationPublisher publisher;

    @Scheduled(cron = "0 * * * * *")
    public void start() {
        log.info("Institution's information processing...");
        List<Institution> institutions = getGlobalFinancesInfo();
        institutions = institutions.subList(0, 50);
        institutionRepository.saveAll(fetchCompaniesStockInfo(institutions))
                .map(institution -> new NotificationDTO(
                        NotificationDTO.NoticeStatus.NEW,
                        NotificationDTO.NoticeType.INFO,
                        LocalDate.now(),
                        LocalDate.now()))
                .collectList().subscribe(publisher::sendNotifications);
        displayTopOrganizations();
    }

    private List<Institution> getGlobalFinancesInfo() {
        log.debug("REST request to get all institutions.");
        WebClient.ResponseSpec responseSpec = webClient.get().uri(uriBuilder -> uriBuilder
                        .pathSegment("/stable/ref-data/symbols")
                        .queryParam("token", appProperties.getIexApiToken()).build())
                .retrieve();
        List<Institution> institutions = responseSpec.bodyToFlux(Institution.class).collectList().block();
        if (institutions != null) {
            log.info("Institutions received {}.", institutions.size());
            return institutions;
        }
        return Collections.emptyList();
    }

    public WebClient.RequestHeadersSpec<?> prepareReceiveCompanyInfoReq(String stock) {
        log.debug("Prepare REST request to receive single institution info by stock {}", stock);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("/stable/stock/".concat(stock.concat("/quote")))
                        .queryParam("token", appProperties.getIexApiToken())
                        .build());
    }

    private Mono<Institution> downloadStockInfo(Institution institution) {
        return
                prepareReceiveCompanyInfoReq(institution.getSymbol())
                        .exchangeToMono(clientResponse -> {
                            if (clientResponse.statusCode().is4xxClientError())
                                log.debug("Stock loading failed. The reason is " + clientResponse.statusCode().getReasonPhrase() + ". " + institution.getSymbol());
                            Mono<StockInfoShortened> res = Mono.empty();
                            if (clientResponse.headers().contentType().isPresent() && MediaType.APPLICATION_JSON_VALUE.contains(clientResponse.headers().contentType().get().getSubtype()))
                                res = clientResponse.bodyToMono(StockInfoShortened.class);
                            return res;
                        })
                        .retryWhen(Retry.backoff(5, Duration.of(1000, ChronoUnit.MILLIS))
                                .filter(error -> error instanceof WebClientResponseException.TooManyRequests || error instanceof WebClientRequestException)
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    publisher.sendNotification(new NotificationDTO(
                                            NotificationDTO.NoticeStatus.NEW,
                                            NotificationDTO.NoticeType.ERROR,
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
                .collect(Collectors.toList()).subList(0, 5)).orElse(Collections.emptyList());
    }

    public List<Institution> highestChangePercentCompanies() {
        return Optional.of(institutionRepository.findAll().toStream()
                .sorted((o1, o2) -> {
                    if (o1.getLastStockInfo().getChangePercent() != null && o2.getLastStockInfo().getChangePercent() != null)
                        return o1.getLastStockInfo().getChangePercent() < o2.getLastStockInfo().getChangePercent() ? 1 : 0;
                    return 1;
                })
                .collect(Collectors.toList()).subList(0, 5)).orElse(Collections.emptyList());
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
        log.info("The highest stock: {} \n ||||||||||||||| \n The gr-st change percent {}2: ", highestStockCompanies(), highestChangePercentCompanies());
    }
}
