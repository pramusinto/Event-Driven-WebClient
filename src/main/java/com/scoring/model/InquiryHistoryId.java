package com.scoring.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Embeddable
public class InquiryHistoryId implements Serializable {
    @Column(name = "inquiry_history_id")
    private Long inquiryHistoryId;
    @Column(name = "inquiry_details_id")
    private Long inquiryDetailsId;
}
