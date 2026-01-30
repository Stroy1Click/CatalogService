package ru.stroy1click.catalog.client;

import ru.stroy1click.catalog.dto.OutboxMessageDto;
import ru.stroy1click.catalog.model.OutboxPublishResult;

public interface OutboxMessageClient {

    OutboxPublishResult send(OutboxMessageDto outboxMessage);
}
