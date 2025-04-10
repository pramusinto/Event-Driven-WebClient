package com.scoring.model.response;

import lombok.Data;

@Data
public class ReportResponse {
    private Long idTempReport;
    private Integer status;
    private String message;
    private Long inquiryId;
    private Long idPefindo;
}
