package ru.stroy1click.catalog.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.client.OutboxMessageClient;
import ru.stroy1click.catalog.dto.OutboxMessageDto;
import ru.stroy1click.catalog.exception.ServiceErrorResponseException;
import ru.stroy1click.catalog.exception.ServiceUnavailableException;
import ru.stroy1click.catalog.entity.OutboxPublishResult;
import ru.stroy1click.catalog.service.outbox.OutboxMessageService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxMessageHandler {

    private final OutboxMessageClient outboxMessageClient;

    private final OutboxMessageService outboxMessageService;

    @Scheduled(fixedDelay = 3_600_000) // 1 час
    public void handle(){
        log.info("handle");
        List<OutboxMessageDto> outboxMessages = this.outboxMessageService.getCreatedAndRetryableMessages();

        if(!outboxMessages.isEmpty()){
            for(OutboxMessageDto outboxMessage : outboxMessages){
                try {
                    OutboxPublishResult result = this.outboxMessageClient.send(outboxMessage);

                    if(result == OutboxPublishResult.PUBLISHED ||
                            result == OutboxPublishResult.ALREADY_PROCESSED){
                        this.outboxMessageService.setSucceededStatus(outboxMessage.getId());
                    }
                } catch (ServiceUnavailableException | ServiceErrorResponseException e){
                    log.error("error ", e);
                    if (outboxMessage.getRetryAttempts() > 1) {
                        outboxMessageService.setRetryStatus(outboxMessage.getId());
                    } else {
                        outboxMessageService.setFailedStatus(outboxMessage.getId(), e.getMessage());
                    }
                } catch (Exception e){
                    log.error("error ", e);
                    outboxMessageService.setFailedStatus(outboxMessage.getId(), e.getMessage());
                }
            }
        }
    }
}
