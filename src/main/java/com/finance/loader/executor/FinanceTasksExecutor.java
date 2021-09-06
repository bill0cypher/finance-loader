
package com.finance.loader.executor;

import com.finance.loader.exceptions.TooManyRequestsException;
import com.finance.loader.model.Institution;
import com.finance.loader.model.StockInfoShortened;
import com.finance.loader.props.IEXTokenHolder;
import com.finance.loader.repo.InstitutionRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FinanceTasksExecutor {

    private final Logger logger = LoggerFactory.getLogger(FinanceTasksExecutor.class.getName());
    private final FinanceLoaderWebClient webClient;
    private final IEXTokenHolder tokenHolder;
    private final InstitutionRepository institutionRepository;

    public FinanceTasksExecutor(FinanceLoaderWebClient webClient, IEXTokenHolder tokenHolder, InstitutionRepository institutionRepository) {
        this.webClient = webClient;
        this.tokenHolder = tokenHolder;
        this.institutionRepository = institutionRepository;
    }

    public void start() {
        List<Institution> institutions = getGlobalFinancesInfo();
        institutions = institutions.subList(0, 100);
        institutionRepository.saveAll(fetchCompaniesStockInfo(institutions)).doOnComplete(this::start).subscribe();
    }

    private List<Institution> getGlobalFinancesInfo() {
        logger.debug("REST request to get all institutions.");
        WebClient.ResponseSpec responseSpec = webClient.getPreparedClient().get().uri(uriBuilder -> uriBuilder
                        .pathSegment("/stable/ref-data/symbols")
                        .queryParam("token", tokenHolder.getApiToken()).build())
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
                        .queryParam("token", tokenHolder.getApiToken())
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
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new TooManyRequestsException(retrySignal.failure().getMessage())))
                        .map(stockInfoShortened -> {
                            if (institution.getStockHistory() == null)
                                institution.setStockHistory(Collections.singletonList(stockInfoShortened));
                            else
                                institution.getStockHistory().add(stockInfoShortened);
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
                .ordered((o1, o2) -> o1.getLastStockInfo().getVolume() > o2.getLastStockInfo().getVolume() ? 1 : 0);
    }

    /*private boolean processInstitutionsImproved(List<Institution> institutions) {
        AtomicInteger integer = new AtomicInteger();
        integer.set(0);
        List<CompletableFuture<Disposable>> tasks = institutions
                .stream().filter(Institution::isEnabled)
                .map((institution) -> {
                    integer.set(integer.get() + 500);
                    if (integer.get() >= 1000)
                        integer.set(0);
                    return CompletableFuture.supplyAsync(() ->
                                    downloadStockInfo(institution),
                            CompletableFuture.delayedExecutor(integer.longValue(), TimeUnit.MILLISECONDS, executorService));
                })
                .collect(Collectors.toList());
        CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
        return true;
    }

    public void execProcessing(List<Institution> institutions) {
        List<CompletableFuture<Boolean>> outerTasks = new ArrayList<>();
        final int ENTITIES_PER_REQUEST = 50;
        int timeout = 0;
        for (int i = 1; i < (institutions.size() / ENTITIES_PER_REQUEST); i++) {
            int finalI = i * ENTITIES_PER_REQUEST;
            timeout+=5;
            if (timeout >= 25)
                timeout = 0;
            if (finalI < institutions.size()) {
                if (i == 1) {
                    outerTasks.add(CompletableFuture.supplyAsync(() ->
                                    processInstitutionsImproved(institutions.subList(0, ENTITIES_PER_REQUEST)),
                            CompletableFuture.delayedExecutor(0, TimeUnit.SECONDS, Executors.newFixedThreadPool(1)) ));
                } else {
                    outerTasks.add(CompletableFuture.supplyAsync(() ->
                                    processInstitutionsImproved(institutions.subList(finalI, finalI + ENTITIES_PER_REQUEST)),
                            CompletableFuture.delayedExecutor(timeout, TimeUnit.SECONDS, Executors.newFixedThreadPool(1))));
                }
            } else {
                logger.error("Out of bounds" + finalI);
            }
        }

        CompletableFuture<Void> tasks = CompletableFuture.allOf(outerTasks.toArray(CompletableFuture[]::new));
        tasks.thenRun(this::start);
        shutdownExecutorService();
    }*/

    /*private void shutdownExecutorService() {
        executorService.shutdown();
        logger.info("ES stopped accepting new tasks...");
        try {
            if (executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                logger.info("ES was disabled successfully.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.warn("Nested threads were interrupted. ES stopped.");
        }
    }*/
}
