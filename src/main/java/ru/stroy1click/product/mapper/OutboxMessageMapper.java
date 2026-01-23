package ru.stroy1click.product.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.stroy1click.product.dto.OutboxMessageDto;
import ru.stroy1click.product.entity.OutboxMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxMessageMapper implements Mappable<OutboxMessage, OutboxMessageDto>{

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    @Override
    public OutboxMessage toEntity(OutboxMessageDto outboxMessageDto) {
        return this.modelMapper.map(outboxMessageDto, OutboxMessage.class);
    }

    @Override
    @SneakyThrows
    public OutboxMessageDto toDto(OutboxMessage outboxMessage) {
        JsonNode payloadNode = this.objectMapper.readTree(outboxMessage.getPayload());
        OutboxMessageDto dto = this.modelMapper.map(outboxMessage, OutboxMessageDto.class);

        dto.setPayload(payloadNode);

        return dto;
    }

    @Override
    public List<OutboxMessageDto> toDto(List<OutboxMessage> e) {
        return e.stream()
                .map(this::toDto)
                .toList();
    }
}
