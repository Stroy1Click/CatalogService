package ru.stroy1click.catalog.client.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import ru.stroy1click.catalog.client.OutboxMessageClient;
import ru.stroy1click.catalog.dto.OutboxMessageDto;
import ru.stroy1click.catalog.exception.OutboxMessageException;
import ru.stroy1click.catalog.exception.ServiceErrorResponseException;
import ru.stroy1click.catalog.exception.ServiceUnavailableException;
import ru.stroy1click.catalog.entity.OutboxPublishResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@CircuitBreaker(name = "outboxClient")
public class OutboxMessageClientImpl implements OutboxMessageClient {

    private final RestClient restClient;

    public OutboxMessageClientImpl(@Value("${url.search}") String url){
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .build();
    }

    @Override
    public OutboxPublishResult send(OutboxMessageDto outboxMessage) {
        log.info("send");
        try {
            return this.restClient.post()
                    .body(outboxMessage)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                        throw new OutboxMessageException(gerErrorMessage(response));
                    }))
                    .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                        throw new ServiceErrorResponseException(gerErrorMessage(response));
                    }))
                    .body(OutboxPublishResult.class);
        } catch (ResourceAccessException e) {
            log.error("get error ", e);
            throw new ServiceUnavailableException("Service unavailable exception");
        }
    }

    private String gerErrorMessage(ClientHttpResponse response) throws IOException {
        return new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}
