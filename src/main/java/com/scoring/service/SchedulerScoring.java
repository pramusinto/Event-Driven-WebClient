package com.scoring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scoring.config.RedisProperties;
import com.scoring.model.TempReport;
import com.scoring.model.dto.SchedulerParams;
import com.scoring.model.event.DebiturEvent;
import com.scoring.model.event.ScoringEvent;
import com.scoring.repository.TempReportRepository;
import com.scoring.service.poc.RedisService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Log4j2
@RequiredArgsConstructor
public class SchedulerScoring {

    private final ScoringService scoringService;
    private final ObjectMapper mapper;
    private final TempReportRepository reportRepository;
    private final RedisService redisService;
    public static Map<Long, SchedulerParams> taskList = new ConcurrentHashMap<>();

    @Scheduled(fixedRateString = "${app.interval-scheduler}")
    public void executeTask() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayDate = LocalDate.now().format(dateFormatter);

        try {
            List<TempReport> removeReportIds = new ArrayList<>();
            taskList.values().stream()
                    .filter(Objects::nonNull)
                    .filter(task -> task.getExpiredDate().equalsIgnoreCase(todayDate))
                    .forEach(task -> {
                        Long tempReportId = task.getEvent().getTempReportId();
                        TempReport tempReport = reportRepository.findByIdReport(tempReportId).orElse(new TempReport());

                        if (redisService.validationOnRedis(tempReportId) || tempReport.getDataScore() != null) {
                            removeReportIds.add(tempReport);
                        }
                    });
            removeReportIds.forEach(report -> {
                SchedulerParams schedulerParams = taskList.get(report.getIdReport());
                checkAndSendEvent(report, schedulerParams);
                taskList.remove(report.getIdReport());
                log.info("[] Release temp-report : {}", report.getIdReport());
            });
        } catch (Exception e){
            log.error("[] Error Scheduler Scoring : {}", e.getMessage(), e);
        }
    }

    private void checkAndSendEvent(TempReport data, SchedulerParams schedulerParams) {
            DebiturEvent debiturEvent = new DebiturEvent();
            try {
                debiturEvent.setEventId(schedulerParams.getEvent().getEventId());
                debiturEvent.setPayload(scoringService.constructPayload(data, HttpStatus.OK.value(), "Success Scoring"));
                scoringService.sendMessageKafka(schedulerParams.getEvent().getNextTopic(), debiturEvent);
                taskList.remove(data.getIdReport());
                log.info("Send topic [{}], event {}", schedulerParams.getEvent().getNextTopic(), debiturEvent.getPayload());
            } catch (JsonProcessingException e) {
                log.error("[] Error when checkAndSendEvent on SchedulerScoring : {}",e.getMessage());
            }
//        }
    }

    public void reloadRetryData(){
        LocalDate currentDate = LocalDate.now();
        List<TempReport> tempReports = reportRepository.reloadRetryReport(currentDate);

        log.debug("[] Reload check data | current-date : {} ", currentDate);
        tempReports.stream()
                .filter(v -> null != v.getMsgQueue())
                .forEach(data -> {
                    try {
                        ScoringEvent event = mapper.readValue(data.getMsgQueue(), ScoringEvent.class);
                        SchedulerParams params = new SchedulerParams();
                        params.setEvent(event);
                        params.setExpiredDate(String.valueOf(currentDate));
                        taskList.put(data.getIdReport(), params);
                    } catch (JsonProcessingException e) {
                        log.error("[] Error parsing : {}", e.getMessage());
                    }

        });
    }


}
