package ru.stroy1click.product.client;

import ru.stroy1click.product.dto.OutboxMessageDto;
import ru.stroy1click.product.model.OutboxPublishResult;

public interface OutboxMessageClient {

    OutboxPublishResult send(OutboxMessageDto outboxMessage);
}
