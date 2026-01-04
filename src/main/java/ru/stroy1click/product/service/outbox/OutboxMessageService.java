package ru.stroy1click.product.service.outbox;

import ru.stroy1click.product.dto.OutboxMessageDto;
import ru.stroy1click.product.entity.OutboxMessage;
import ru.stroy1click.product.model.MessageType;

import java.util.List;

public interface OutboxMessageService {

    void save(Object entity, MessageType messageType);

    List<OutboxMessageDto> getCreatedAndRetryableMessages();

    void setSucceededStatus(Long id);

    void setFailedStatus(Long id, String errorMessage);

    void setRetryStatus(Long id);
}
