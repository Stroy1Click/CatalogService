package ru.stroy1click.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.stroy1click.product.model.MessageStatus;
import ru.stroy1click.product.model.MessageType;

import java.time.LocalDateTime;

@Data
@Table(schema = "product", name = "outbox_message")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String payload;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private String errorMessage;

    private int retryAttempts;

}
