package com.scoring.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scoring.model.DebiturScoringRequest;
import com.scoring.model.event.DebiturEvent;
import com.scoring.service.ScoringService;
import com.scoring.service.poc.ScoringFailedService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/product/scoring")
@AllArgsConstructor
@Log4j2
public class TestController {

    private final ScoringService scoringService;
    private final ScoringFailedService scoringFailedService;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private ObjectMapper mapper;

    @GetMapping
    public SseEmitter getTestingDataScoring(@RequestParam("pefindoId") Long pefindoId,
                                                @RequestParam("idTempReport") Integer idTempReport){
        SseEmitter emitter = new SseEmitter(0L);
        DebiturEvent event = getDebiturEvent(pefindoId, idTempReport);

        try {
            emitter.send(SseEmitter.event()
                    .name("Accepted")
                    .data("Accepted"));

            Map<String, Object> directReportScoring = scoringService.findDirectReportScoring(event, emitter);
            emitter.send(SseEmitter.event()
                    .name("Scoring")
                    .data(directReportScoring));
            emitter.complete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return emitter;
    }

    private DebiturEvent getDebiturEvent(Long pefindoId, Integer idTempReport) {
        try {
            DebiturScoringRequest request = new DebiturScoringRequest();
            request.setIdPefindo(pefindoId);
            request.setIdTempReport(idTempReport);

            DebiturEvent event = new DebiturEvent();
            event.setEventId(UUID.randomUUID().toString());
            event.setPayload(mapper.writeValueAsString(request));

            return event;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/get-500")
    public SseEmitter getTesting500DataScoring(@RequestParam("pefindoId") Long pefindoId,
                                            @RequestParam("idTempReport") Integer idTempReport){
        SseEmitter emitter = new SseEmitter(0L);
        DebiturEvent event = getDebiturEvent(pefindoId, idTempReport);

        try {
            emitter.send(SseEmitter.event()
                    .name("Accepted")
                    .data("Accepted"));

            Map<String, Object> directReportScoring = scoringFailedService.findDirectReportScoringFailed(event, emitter);
            emitter.send(SseEmitter.event()
                    .name("Scoring")
                    .data(directReportScoring));
            emitter.complete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return emitter;
    }
}
