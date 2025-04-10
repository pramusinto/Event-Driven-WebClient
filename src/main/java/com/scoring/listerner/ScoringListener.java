package com.scoring.listerner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scoring.model.event.ScoringEvent;
import com.scoring.service.ScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ScoringListener {

    private final AsyncScoringListen asyncScoringListen;

    @KafkaListener(topics = "${app.topic.listen-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void scoringListener(String event) {
        asyncScoringListen.executeEvent(event);
    }
}
