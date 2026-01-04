package ru.stroy1click.product.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.stroy1click.product.dto.OutboxMessageDto;
import ru.stroy1click.product.entity.OutboxMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxMessageMapper implements Mappable<OutboxMessage, OutboxMessageDto>{

    private final ModelMapper modelMapper;

    @Override
    public OutboxMessage toEntity(OutboxMessageDto outboxMessageDto) {
        return this.modelMapper.map(outboxMessageDto, OutboxMessage.class);
    }

    @Override
    public OutboxMessageDto toDto(OutboxMessage outboxMessage) {
        return this.modelMapper.map(outboxMessage, OutboxMessageDto.class);
    }

    @Override
    public List<OutboxMessageDto> toDto(List<OutboxMessage> e) {
        return e.stream()
                .map(message -> this.modelMapper.map(message, OutboxMessageDto.class))
                .toList();
    }
}
