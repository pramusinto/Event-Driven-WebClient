package com.scoring.model.dto;

import com.scoring.model.event.DebiturEvent;
import com.scoring.model.response.Token;
import lombok.Data;

@Data
public class TestRequestDto {
    private DebiturEvent event;
    private Token token;
}
