package com.scoring.listerner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scoring.model.event.ScoringEvent;
import com.scoring.service.ScoringService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Data
@Log4j2
@Service
@RequiredArgsConstructor
public class AsyncScoringListen {

    private final ScoringService service;
    private final ObjectMapper mapper;

    @Async("kafkaTaskExecutorProduct")
    public void executeEvent(String event){
        try {
            ScoringEvent debiturEvent = mapper.readValue(event, ScoringEvent.class);
            log.info("Event Consume : {}", event);

            service.getDataScoring(debiturEvent);
        } catch (JsonProcessingException e) {
            log.error("{}", e.getMessage(), e);
        }
    }
}
