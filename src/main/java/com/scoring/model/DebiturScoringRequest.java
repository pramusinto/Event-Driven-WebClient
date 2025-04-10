package com.scoring.model;

import lombok.Data;

@Data
public class DebiturScoringRequest {
    private Long idPelapor;
    private Long idPefindo;
    private Long noIdentitas;
    private Integer idTempReport;
}
