package com.scoring.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

@Data
@Entity
@Table(name = "inquiry_details", schema = "staging")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class InquiryDetails {
    @EmbeddedId
    private InquiryHistoryId id;

    private String eventId;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> parameters;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> mappedParams;

//    @OneToOne(fetch = EAGER, cascade = ALL, mappedBy = "inquiryDetails")
//    private TempReport reportData;

    @CreationTimestamp
    private LocalDate createdDate;
    private Timestamp reportGeneratedAt;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> scoringParams;

    public Long getInquiryId() {
        return id.getInquiryDetailsId();
    }
}
