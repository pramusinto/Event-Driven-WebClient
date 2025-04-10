package com.scoring.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class Scoring {
    private String period;
    private String id_pefindo;
    private Object score;
    private Double pod;
    private List<String> reason_code;
    private List<String> reason_desc;
    private String risk_grade;
    private String risk_desc;
}
