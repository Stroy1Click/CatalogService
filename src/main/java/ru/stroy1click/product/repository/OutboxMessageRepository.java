package ru.stroy1click.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stroy1click.product.entity.OutboxMessage;
import ru.stroy1click.product.model.MessageStatus;

import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findTop2ByStatusOrStatus(MessageStatus status, MessageStatus messageStatus);
}
