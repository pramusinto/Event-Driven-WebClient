package com.scoring.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "master_product", schema = "master")
public class MasterProduct {

    @Id
    private Long productId;
    private String productName;
    private Long scoringModelId;
    private Long dataSourceId;
    private Long productParamId;
    private Long reportTemplateId;
    private String scoringModelUrl;
    private String scoringModelName;
    private String scoringBulkId;
    private String scoringBulkName;
}
