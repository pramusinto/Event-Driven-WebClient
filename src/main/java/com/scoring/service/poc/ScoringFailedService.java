package com.scoring.service.poc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scoring.config.AppConfig;
import com.scoring.model.DebiturScoringRequest;
import com.scoring.model.InquiryDetails;
import com.scoring.model.TempReport;
import com.scoring.model.dto.InternalStatus;
import com.scoring.model.dto.SchedulerParams;
import com.scoring.model.event.DebiturEvent;
import com.scoring.model.event.ScoringEvent;
import com.scoring.model.response.ReportResponse;
import com.scoring.model.response.Token;
import com.scoring.repository.InquiryDetailsRepository;
import com.scoring.repository.TempReportRepository;
import com.scoring.service.SchedulerScoring;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@Log4j2
@Service
@RequiredArgsConstructor
public class ScoringFailedService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TempReportRepository reportRepository;
    private final ObjectMapper mapper;
    private final AppConfig appConfig;
    private final WebClient.Builder webClientBuilder;
    private final InquiryDetailsRepository detailsRepo;
    private final RedisService redisService;
    public Map<String, SseEmitter> emitters = new HashMap<>();

    public Map<String, Object> findDirectReportScoringFailed(DebiturEvent debiturEvent, SseEmitter emitter) {
        emitters.put(debiturEvent.getEventId(),emitter);

        try {
            DebiturScoringRequest payload = mapper.readValue(debiturEvent.getPayload(), DebiturScoringRequest.class);
            ScoringEvent event = new ScoringEvent();
            event.setEventId(debiturEvent.getEventId());
            event.setTempReportId(Long.valueOf(payload.getIdTempReport()));
            event.setIdPefindo(String.valueOf(payload.getIdPefindo()));
            event.setNextTopic("scoring-pub-test");

            String token = getTokenUpdate(); //getToken(username, password, debiturEvent.getEventId());
            if (null != token) {
                log.debug("[{}] access_token : {}", event.getEventId(), token);
                TempReport tempReport = reportRepository.findByIdReport(event.getTempReportId()).orElse(null);
                String result = triggerScoringUpdate(event, token, tempReport);
                return StringUtils.isBlank(result) ? Map.of() : mapper.readValue(result, new TypeReference<>() {});
            } else {
                log.info("Token is Null");
            }
            log.info("Send topic [{}], event {}", event.getNextTopic(), debiturEvent.getPayload());
        } catch (IOException e) {
            /* todo **/
            log.error("[] Error when get scoring {}", e.getMessage());
        }
        return Map.of();
    }

    public String triggerScoringUpdate(ScoringEvent event, String token, TempReport tempReport){
        if (null != tempReport){
            try {
                return requestForScoringEngine(event, tempReport, token);
            } catch (ReadTimeoutException ex) {
                Throwable cause = ex.getCause();
                cause = cause == null ? ex : cause;

                if(cause instanceof TimeoutException) {
                    log.error("[{}] Timeout with request {}",event.getEventId(),event.getTempReportId());
                }else{
                    log.error("[{}] ReadTimeoutException {}",event.getEventId(),event.getTempReportId());
                }
                SchedulerScoring.taskList.computeIfAbsent(tempReport.getIdReport(), k -> getParams(event));
            }
        }
        return null;
    }

    private String getTokenUpdate(){
        String basicAuthHeader = "basic " + Base64Utils.encodeToString(
                (appConfig.getAuth().getUsername() + ":" + appConfig.getAuth().getPassword()).getBytes());
        log.debug("[] Auth : {}", basicAuthHeader);

        String getTokenUrl = UriComponentsBuilder.fromUriString(appConfig.getScoringUrl()+"/SASLogon/oauth/token")
                .build()
                .toUriString();
        log.info("[] POST {} \n --body {}", getTokenUrl, Map.of("username", appConfig.getAuth().getBodyUsername(),
                "password", appConfig.getAuth().getBodyPassword()));

        String responseToken = webClientBuilder.build().post()
                .uri(getTokenUrl)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                .cookie("sas-ingress-nginx", "32a9150a76557aff5f2a6f8779f9cf70|20e3ee4727b26bafdc0dffaf79619729")
                .cookie("JSESSIONID", "654BDCD73FDCC3D0CC874B7CB23B525A")
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("username", appConfig.getAuth().getBodyUsername())
                        .with("password", appConfig.getAuth().getBodyPassword()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            Token token = mapper.readValue(responseToken, new TypeReference<>() {});
            return token.getAccess_token();
        } catch (JsonProcessingException e) {
            log.error("Error when get token !", e);
        }
        return null;
    }

    private String requestForScoringEngine(ScoringEvent event, TempReport tempReport, String token) {
        String bearerAuth = "Bearer " + token;
        String pathUrl = StringUtils.isNotBlank(appConfig.getScoringDefaultPath()) ? appConfig.getScoringDefaultPath() :
                "/microanalyticScore/modules/idscore_rtlcor_scoringengine_bigreport/steps/execute";

        if (tempReport.getMasterProduct() != null){
            pathUrl = tempReport.getMasterProduct().getScoringModelUrl();
        }

        String getModelUrl = UriComponentsBuilder.fromUriString(pathUrl)
                .build()
                .toUriString();

        Map<String, Object> request = generateScoringRequest(event.getIdPefindo(), tempReport);
        String resultCommonReport = getResultCommonReport(event, appConfig.getScoringUrl() + getModelUrl, bearerAuth, request, tempReport);
        try {
            String result = mapper.writeValueAsString(resultCommonReport);
            log.debug("[] Result-String {}", result);
            if (result.contains("process failed") || StringUtils.isBlank(resultCommonReport)){
                log.info("[{}] Response Scoring Failed", tempReport.getIdReport());
                SchedulerScoring.taskList.remove(tempReport.getIdReport());
                saveAndSendErrorNotif(event, tempReport, HttpStatus.INTERNAL_SERVER_ERROR.value(), InternalStatus.INTERNAL_SERVER_ERROR.getLabel());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return resultCommonReport;
    }

    private Map<String, Object> generateScoringRequest(String pefindoId, TempReport tempReport) {
        List<Map<String, Object>> inputs = createBodyRequest(pefindoId, tempReport);
        return Map.of("inputs", inputs);
    }

    private List<Map<String, Object>> createBodyRequest(String pefindoId, TempReport tempReport){
        List<Map<String, Object>> bodyRquest = new java.util.ArrayList<>(List.of(
                Map.of(
                        "name", "id_pefindo_",
                        "value", null == pefindoId ? "" : pefindoId),
                Map.of(
                        "name", "id_report_",
                        "value", null == tempReport.getIdReport() ? 0 : tempReport.getIdReport())));

        InquiryDetails inqDetails = detailsRepo.findByIdInquiryDetailsId(tempReport.getId().getInquiryDetailsId());
        Map<String, Object> scoringParams = inqDetails.getScoringParams();
        if(null != scoringParams && !scoringParams.isEmpty()){
            for(Map.Entry<String, Object> param : scoringParams.entrySet()){
                bodyRquest.add(Map.of("name", param.getKey(), "value", param.getValue()));
            }
        }
        return bodyRquest;
    }

    private SchedulerParams getParams(ScoringEvent event) {
        SchedulerParams setup = new SchedulerParams();
        setup.setExpiredDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        setup.setEvent(event);
        return setup;
    }

    private String getResultCommonReport(ScoringEvent event, String scoringUrl, String bearerAuth,
                                         Map<String, Object> request, TempReport tempReport) {
        String finalScoringUrl = scoringUrl+"/500";
        log.info("[{}] Call : {} \n --data: {}", event.getEventId(), finalScoringUrl, request);
        try {
            return webClientBuilder.build().post()
                    .uri(finalScoringUrl)
                    .header(HttpHeaders.AUTHORIZATION, bearerAuth)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    //                .header(HttpHeaders.ACCEPT, "application/vnd.sas.microanalytic.module.step.output+json")
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
//                    .onStatus(HttpStatus::is5xxServerError, loggingServerError(event, tempReport))
                    .onStatus(HttpStatus::is4xxClientError, loggingBadRequest(event, tempReport))
                    .onStatus(HttpStatus::is2xxSuccessful, updateAndResponseScoring(event))
                    .bodyToMono(String.class)
                    .retryWhen(Retry
                            .fixedDelay(appConfig.getWebclientRetryCount(), Duration.ofMillis(appConfig.getWebclientRetryDelay()))
                            .filter(throwable -> throwable instanceof WebClientResponseException && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                            .doBeforeRetry(retrySignal -> log.warn("[{}] Retry attempt {} for URL: {}",
                                    event.getEventId(), retrySignal.totalRetries() + 1, finalScoringUrl))
                    ).onErrorResume(e -> {
                        log.error("Retries exhausted for request: {}", e.getMessage());
                        loggingServerError(event, tempReport);
                        return Mono.empty();
                    })
                    .toFuture().get();
        } catch (Exception e) {
            log.error("[{}] Unexpected error: {}", event.getEventId(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void saveAndSendErrorNotif(ScoringEvent event, TempReport tempReport, int rawStatusCode
            , String errorMsg) {
        log.info("[{}] TempReportId {} error-status {} ", event.getEventId(),
                event.getTempReportId(), rawStatusCode);
        tempReport.setScoringStatus(InternalStatus.INTERNAL_SERVER_ERROR);
        tempReport.setStatusValue(errorMsg);
        reportRepository.save(tempReport);

        try {
            DebiturEvent debiturEvent = new DebiturEvent();
            debiturEvent.setEventId(event.getEventId());
            debiturEvent.setPayload(constructPayload(tempReport, rawStatusCode, errorMsg));
            sendMessageKafka(event.getNextTopic(), debiturEvent);
            log.info("Send topic [{}], event {}", event.getNextTopic(), debiturEvent.getPayload());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Function<ClientResponse, Mono<? extends Throwable>> loggingBadRequest(ScoringEvent event, TempReport tempReport) {
        return response ->
                response.bodyToMono(String.class)
                        .flatMap(body -> {
                            saveAndSendErrorNotif(event, tempReport, HttpStatus.BAD_REQUEST.value(), body);
                            return Mono.empty();
                        });
    }

    private Function<ClientResponse, Mono<? extends Throwable>> updateAndResponseScoring(ScoringEvent event) {
        return response -> {
            try {
                Long tempReportId = event.getTempReportId();
                log.debug("[{}] response : {}", event.getEventId(), response.rawStatusCode());
                TempReport tempReport = reportRepository.findByIdReport(tempReportId).orElse(null);

                if (null != tempReport){
                    checkAndSendMessageResponse(event, tempReportId, tempReport);
                    tempReport.setScoringStatus(InternalStatus.PROCESSED);
                    tempReport.setStatusValue(InternalStatus.PROCESSED.getLabel());
                    tempReport.setMsgQueue(mapper.writeValueAsString(event));
                    reportRepository.save(tempReport);
                }
            } catch(JsonProcessingException e){
                log.info("[] Error when parse event to string");
                log.error(e.getMessage());
            }
            return Mono.empty();
        };
    }

    private void checkAndSendMessageResponse(ScoringEvent event, Long tempReportId, TempReport tempReport) throws JsonProcessingException {
        if (!redisService.validationOnRedis(tempReportId) && tempReport.getDataScore() == null) {
            SchedulerParams value = getParams(event);
            SchedulerScoring.taskList.computeIfAbsent(tempReport.getIdReport(), k -> value);
            log.info("Put to Scheduler report-id : {}, params {}", tempReport.getIdReport(), value);
        } else {
            DebiturEvent debiturEvent = new DebiturEvent();
            debiturEvent.setEventId(event.getEventId());
            debiturEvent.setPayload(constructPayload(tempReport, HttpStatus.OK.value(), "Success Scoring"));
            sendMessageKafka(event.getNextTopic(), debiturEvent);
            log.info("Send topic [{}], event {}", event.getNextTopic(), debiturEvent.getPayload());
        }
    }

    public String constructPayload(TempReport data, Integer statusScoring, String msgScoring) throws JsonProcessingException {
        ReportResponse response = new ReportResponse();
        response.setIdTempReport(data.getIdReport());
        response.setStatus(statusScoring);
        response.setMessage(msgScoring);
        response.setInquiryId(data.getId().getInquiryDetailsId());
        return mapper.writeValueAsString(response);
    }

    public <T> void sendMessageKafka(String topic, T message){
        try {
            kafkaTemplate.send(topic, mapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void loggingServerError(ScoringEvent event, TempReport tempReport) {
        int ordinal = HttpStatus.INTERNAL_SERVER_ERROR.value();
        log.warn("[{}] Server error after retries. Status code: {}", event.getEventId(), ordinal);
        saveAndSendErrorNotif(event, tempReport, ordinal, InternalStatus.INTERNAL_SERVER_ERROR.getLabel());
    }
}
