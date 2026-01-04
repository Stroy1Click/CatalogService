package ru.stroy1click.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.stroy1click.product.model.MessageStatus;
import ru.stroy1click.product.model.MessageType;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxMessageDto {

    private Long id;

    private String payload;

    private LocalDateTime createdAt;

    private MessageStatus status;

    private MessageType type;

    private String errorMessage;

    private int retryAttempts;
}
