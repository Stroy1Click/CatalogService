package ru.stroy1click.catalog.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.stroy1click.catalog.entity.MessageStatus;
import ru.stroy1click.catalog.entity.MessageType;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxMessageDto {

    private Long id;

    private JsonNode payload;

    private LocalDateTime createdAt;

    private MessageStatus status;

    private MessageType type;

    private String errorMessage;

    private int retryAttempts;
}
