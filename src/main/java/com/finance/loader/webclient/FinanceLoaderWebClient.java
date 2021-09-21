package com.finance.loader.webclient;

import com.finance.loader.props.IEXPropsHolder;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class FinanceLoaderWebClient {

    private WebClient preparedClient;
    private final IEXPropsHolder tokenHolder;

    public FinanceLoaderWebClient(IEXPropsHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }

    @PostConstruct
    public void init() {
        preparedClient = createWebClient();
    }

    private ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("elastic")
                .pendingAcquireMaxCount(5000)
                .maxConnections(5000)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();
    }
    private HttpClient createHttpClient() {
        return HttpClient.create(connectionProvider())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(conn ->
                        conn
                                .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))
                );
    }

    private WebClient createWebClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(createHttpClient()))
                .baseUrl(tokenHolder.getBaseResource())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", tokenHolder.getBaseResource()))
                .build();
    }

    public WebClient getPreparedClient() {
        return preparedClient;
    }

}
