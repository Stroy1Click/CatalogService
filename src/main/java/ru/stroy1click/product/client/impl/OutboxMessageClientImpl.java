package ru.stroy1click.product.client.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import ru.stroy1click.product.client.OutboxMessageClient;
import ru.stroy1click.product.dto.OutboxMessageDto;
import ru.stroy1click.product.exception.AlreadyExistsException;
import ru.stroy1click.product.exception.OutboxMessageException;
import ru.stroy1click.product.exception.ServiceErrorResponseException;
import ru.stroy1click.product.exception.ServiceUnavailableException;
import ru.stroy1click.product.model.OutboxPublishResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@CircuitBreaker(name = "outboxClient")
public class OutboxMessageClientImpl implements OutboxMessageClient {

    private final RestClient restClient;
    private final StatusAggregator statusAggregator;

    public OutboxMessageClientImpl(@Value("${url.search}") String url, StatusAggregator statusAggregator){
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .build();
        this.statusAggregator = statusAggregator;
    }

    @Override
    public OutboxPublishResult send(OutboxMessageDto outboxMessage) {
        log.info("registration");
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
