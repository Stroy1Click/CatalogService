package ru.stroy1click.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stroy1click.catalog.entity.OutboxMessage;
import ru.stroy1click.catalog.entity.MessageStatus;

import java.util.Collection;
import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findAllByStatusIn(Collection<MessageStatus> statuses);
}
