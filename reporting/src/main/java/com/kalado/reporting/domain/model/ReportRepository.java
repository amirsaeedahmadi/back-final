package com.kalado.reporting.domain.model;

import com.kalado.common.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReporterId(Long reporterId);
    List<Report> findByStatus(ReportStatus status);

    @Query(value = "SELECT * FROM reports r WHERE " +
            "(:startDate IS NULL OR r.created_at >= :startDate) AND " +
            "(:endDate IS NULL OR r.created_at <= :endDate) AND " +
            "(:violationType IS NULL OR r.violation_type = :violationType)",
            nativeQuery = true)
    List<Report> findByDateRangeAndType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("violationType") String violationType);

    List<Report> findByCreatedAtBetweenAndViolationTypeContainingIgnoreCase(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String violationType);

    List<Report> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}