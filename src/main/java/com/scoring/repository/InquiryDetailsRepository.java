package com.scoring.repository;

import com.scoring.model.InquiryDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryDetailsRepository extends JpaRepository<InquiryDetails, Long> {
    InquiryDetails findByIdInquiryDetailsId(Long inquiryId);
}
