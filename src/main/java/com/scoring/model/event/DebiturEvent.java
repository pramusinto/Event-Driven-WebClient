package com.scoring.model.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DebiturEvent {
    private String eventId;
    private String payload;
}
