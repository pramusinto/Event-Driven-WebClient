package com.scoring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scoring.model.dto.InternalStatus;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Map;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

@Data
@Entity
@Table(name = "temp_report", schema = "staging")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class TempReport {

    @EmbeddedId
    private InquiryHistoryId id;

    @Column(name = "id_report")
    private Long idReport;
//    private Integer idStatusReport;
//    private Long idProdukReport;
//    private Long idTipeDebitur;
//    private Long idPelapor;
//
//    @Type(type = "jsonb")
//    @Column(columnDefinition = "jsonb")
//    private Map<String, Object> parameter;
//
//    @Type(type = "jsonb")
//    @Column(columnDefinition = "jsonb")
//    private Map<String, Object> dataReport;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Object[] dataScore;

//    private String createdBy;
//    private Timestamp createdDate;
//    private String updatedBy;
//    private Timestamp updatedDate;
    @Enumerated(EnumType.ORDINAL)
    private InternalStatus scoringStatus;
    private String statusValue;
    private String msgQueue;

//    private Boolean isBigReport;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="idProdukReport",referencedColumnName="productId",insertable = false,updatable = false)
    private MasterProduct masterProduct;

//    @JsonIgnore
//    @OneToOne(fetch = EAGER, cascade = ALL)
//    @JoinColumns({
//            @JoinColumn(
//                    updatable=false, insertable=false,
//                    name="inquiry_history_id",
//                    referencedColumnName="inquiry_history_id"),
//            @JoinColumn(
//                    updatable=false, insertable=false,
//                    name="inquiry_details_id",
//                    referencedColumnName="inquiry_details_id"),
//    })
//    private InquiryDetails inquiryDetails;
}