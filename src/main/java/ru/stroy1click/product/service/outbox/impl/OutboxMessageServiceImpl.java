package ru.stroy1click.product.service.outbox.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stroy1click.product.dto.OutboxMessageDto;
import ru.stroy1click.product.entity.OutboxMessage;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.OutboxMessageMapper;
import ru.stroy1click.product.model.MessageStatus;
import ru.stroy1click.product.model.MessageType;
import ru.stroy1click.product.repository.OutboxMessageRepository;
import ru.stroy1click.product.service.outbox.OutboxMessageService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OutboxMessageServiceImpl implements OutboxMessageService {

    private final OutboxMessageRepository outboxMessageRepository;

    private final ObjectMapper objectMapper;

    private final OutboxMessageMapper outboxMessageMapper;

    @Override
    @Transactional
    public void save(Object entity, MessageType messageType) {
        try {
            this.outboxMessageRepository.save(new ru.stroy1click.product.entity.OutboxMessage(
                    null, objectMapper.writeValueAsString(entity), LocalDateTime.now(), MessageStatus.CREATED, messageType, "", 3
            ));
        } catch (JsonProcessingException e) {
            log.error("json processing error ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<OutboxMessageDto> getCreatedAndRetryableMessages() {
        return this.outboxMessageMapper.toDto(
                this.outboxMessageRepository.findTop2ByStatusOrStatus(MessageStatus.CREATED, MessageStatus.RETRYABLE)
        );
    }

    @Override
    @Transactional
    public void setSucceededStatus(Long id) {
        OutboxMessage outboxMessage = this.outboxMessageRepository.findById(id).orElseThrow(
                () -> new NotFoundException("OutboxMessage not found with id {}" + id));

        outboxMessage.setStatus(MessageStatus.SUCCEEDED);
    }

    @Override
    @Transactional
    public void setFailedStatus(Long id, String errorMessage) {
        OutboxMessage outboxMessage = this.outboxMessageRepository.findById(id).orElseThrow(
                () -> new NotFoundException("OutboxMessage not found with id {}" + id));

        outboxMessage.setStatus(MessageStatus.FAILED);
        outboxMessage.setErrorMessage(errorMessage);
    }

    @Override
    @Transactional
    public void setRetryStatus(Long id) {
        OutboxMessage outboxMessage = this.outboxMessageRepository.findById(id).orElseThrow(
                () -> new NotFoundException("OutboxMessage not found with id {}" + id));

        outboxMessage.setStatus(MessageStatus.RETRYABLE);
        outboxMessage.setRetryAttempts(outboxMessage.getRetryAttempts() - 1);
    }
}
