package com.scoring.model.response;

import lombok.Data;

@Data
public class ReportResponseDirect {
    private String eventId;
    private Object report;
    private Object scoring;
}
