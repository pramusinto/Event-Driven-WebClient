package com.scoring.model.dto;

import com.scoring.model.event.ScoringEvent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SchedulerParams {
    private String expiredDate;
    private ScoringEvent event;
}
