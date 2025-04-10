package com.scoring.model.event;

import lombok.Data;

@Data
public class ScoringEvent {
    private String eventId;
    private String idPefindo;
    private Long tempReportId;
    private String nextTopic;
}
