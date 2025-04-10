package com.scoring.repository;

import com.scoring.model.InquiryHistoryId;
import com.scoring.model.TempReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TempReportRepository extends JpaRepository<TempReport, InquiryHistoryId> {
    @Query(value = " SELECT inquiry_history_id, inquiry_details_id, " +
            " id_report, data_score, scoring_status, status_value, msg_queue, id_produk_report " +
            " FROM staging.temp_report tr WHERE tr.created_date > :currentDate \n" +
            " AND tr.data_score IS NULL \n" +
            " AND tr.scoring_status NOT IN (2, 3) " +
            " AND tr.msg_queue IS NOT NULL ", nativeQuery = true)
    List<TempReport> reloadRetryReport(@Param("currentDate") LocalDate currentDate);

    Optional<TempReport> findByIdReport(Long idReport);
}
