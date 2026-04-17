package com.epit.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Spring Boot → FastAPI AI 서버 통신 설정.
 *
 * 통신 패턴:
 *   1. [동기 요청] WebClient.block() — 즉각적인 예측 결과가 필요할 때
 *   2. [비동기 요청] WebClient + Mono/Flux — 배치 처리, 비차단 파이프라인
 *   3. [이벤트 스트림] Server-Sent Events (SSE) — 실시간 감지 결과 수신
 */
@Configuration
public class WebClientConfig {

    @Value("${ai-server.url}")
    private String aiServerUrl;

    @Value("${ai-server.timeout-seconds:30}")
    private int timeoutSeconds;

    @Bean("aiServerWebClient")
    public WebClient aiServerWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .responseTimeout(Duration.ofSeconds(timeoutSeconds))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .baseUrl(aiServerUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            // 프로덕션에서는 민감 정보 제외 후 로깅
            return Mono.just(req);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> Mono.just(res));
    }
}
