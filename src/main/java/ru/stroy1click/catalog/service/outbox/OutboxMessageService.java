package ru.stroy1click.catalog.service.outbox;

import ru.stroy1click.catalog.dto.OutboxMessageDto;
import ru.stroy1click.catalog.entity.MessageType;

import java.util.List;

public interface OutboxMessageService {

    void save(Object entity, MessageType messageType);

    List<OutboxMessageDto> getCreatedAndRetryableMessages();

    void setSucceededStatus(Long id);

    void setFailedStatus(Long id, String errorMessage);

    void setRetryStatus(Long id);
}
