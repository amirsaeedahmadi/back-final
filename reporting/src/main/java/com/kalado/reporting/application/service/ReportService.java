package com.kalado.reporting.application.service;

import com.kalado.common.dto.*;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.ProductStatus;
import com.kalado.common.enums.ReportStatus;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.common.feign.user.UserApi;
import com.kalado.reporting.domain.model.Report;
import com.kalado.reporting.domain.model.ReportMapper;
import com.kalado.reporting.domain.model.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
  private final ReportRepository reportRepository;
  private final UserApi userApi;
  private final EvidenceService evidenceService;
  private final EmailService emailService;
  private final ReportMapper reportMapper;
  private final ProductApi productApi;

  @Transactional
  public ReportResponseDto createReport(ReportCreateRequestDto request, Long reporterId) {
    validateRequest(request);
    validateUser(reporterId);

    ProductDto product = productApi.getProduct(request.getReportedContentId());
    if (product == null) {
      throw new CustomException(ErrorCode.NOT_FOUND, "Product not found");
    }
    Long reportedUserId = product.getSellerId();
    validateUser(reportedUserId);

    if (reporterId.equals(reportedUserId)) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Cannot report your own product");
    }

    Report report = reportMapper.toReport(request, reporterId);
    report.setReportedUserId(reportedUserId);

    if (request.getEvidenceFiles() != null && !request.getEvidenceFiles().isEmpty()) {
      List<String> evidenceUrls = evidenceService.storeEvidence(request.getEvidenceFiles());
      report.setEvidenceFiles(evidenceUrls);
    }

    Report savedReport = reportRepository.save(report);
    emailService.sendReportConfirmation(reporterId);

    log.info("Created report ID: {} for product ID: {}", savedReport.getId(), request.getReportedContentId());

    return reportMapper.toReportResponse(savedReport);
  }

  @Transactional
  public ReportResponseDto updateReportStatus(Long reportId, ReportStatusUpdateDto request, Long adminId) {
    Report report = getReportById(reportId);

    report.setStatus(request.getStatus());
    report.setAdminId(adminId);
    report.setLastUpdatedAt(LocalDateTime.now());
    report.setAdminNotes(request.getAdminNotes());

    if (request.isBlockUser()) {
      try {
        userApi.blockUser(report.getReportedUserId());
        report.setUserBlocked(true);
        List<ProductDto> userProducts = productApi.getSellerProducts(report.getReportedUserId());
        for (ProductDto product : userProducts) {
          try {
            productApi.updateProductStatus(
                    product.getId(),
                    report.getReportedUserId(),
                    new ProductStatusUpdateDto(ProductStatus.DELETED)
            );
          } catch (Exception e) {
            log.error("Failed to block product {} of blocked user {}",
                    product.getId(), report.getReportedUserId(), e);
          }
        }
      } catch (Exception e) {
        log.error("Failed to block user: {}", report.getReportedUserId(), e);
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to block user");
      }
    }

    if (request.isBlockProduct() && report.getReportedContentId() != null) {
      try {
        productApi.updateProductStatus(
                report.getReportedContentId(),
                report.getReportedUserId(),
                new ProductStatusUpdateDto(ProductStatus.DELETED)
        );
      } catch (Exception e) {
        log.error("Failed to block product: {}", report.getReportedContentId(), e);
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to block product");
      }
    }

    Report updatedReport = reportRepository.save(report);
    return reportMapper.toReportResponse(updatedReport);
  }


  private Report getReportById(Long reportId) {
    return reportRepository
        .findById(reportId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Report not found"));
  }

  private void validateUser(Long userId) {
    try {
      UserDto user = userApi.getUserProfile(userId);
      if (user == null) {
        throw new CustomException(ErrorCode.NOT_FOUND, "User not found");
      }
    } catch (Exception e) {
      log.error("Error validating user: {}", userId, e);
      throw new CustomException(ErrorCode.NOT_FOUND, "User not found");
    }
  }

  private void validateRequest(ReportCreateRequestDto request) {
    if (request == null) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Request cannot be null");
    }
    if (request.getViolationType() == null || request.getViolationType().trim().isEmpty()) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Violation type is required");
    }
    if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Description is required");
    }
    if (request.getReportedContentId() == null) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Product ID is required");
    }
  }

  @Transactional(readOnly = true)
  public ReportStatisticsDto getStatistics(
          LocalDateTime startDate,
          LocalDateTime endDate,
          String violationType) {

    List<Report> reports;
    if (startDate != null && endDate != null) {
      if (violationType != null && !violationType.trim().isEmpty()) {
        reports = reportRepository.findByCreatedAtBetweenAndViolationTypeContainingIgnoreCase(
                startDate, endDate, violationType);
      } else {
        reports = reportRepository.findByCreatedAtBetween(startDate, endDate);
      }
    } else {
      reports = reportRepository.findAll();
    }

    long totalReports = reports.size();
    Map<ReportStatus, Long> statusCounts = reports.stream()
            .collect(Collectors.groupingBy(Report::getStatus, Collectors.counting()));

    double averageResolutionTime = reports.stream()
            .filter(report -> report.getStatus() == ReportStatus.RESOLVED)
            .mapToDouble(report -> {
              Duration duration = Duration.between(report.getCreatedAt(), report.getLastUpdatedAt());
              return duration.toHours();
            })
            .average()
            .orElse(0.0);

    Map<String, Long> reportsByType = reports.stream()
            .collect(Collectors.groupingBy(Report::getViolationType, Collectors.counting()));

    Map<String, Long> reportsByStatus = reports.stream()
            .collect(Collectors.groupingBy(r -> r.getStatus().toString(), Collectors.counting()));

    return ReportStatisticsDto.builder()
            .totalReports(totalReports)
            .pendingReports(statusCounts.getOrDefault(ReportStatus.SUBMITTED, 0L))
            .resolvedReports(statusCounts.getOrDefault(ReportStatus.RESOLVED, 0L))
            .rejectedReports(statusCounts.getOrDefault(ReportStatus.REJECTED, 0L))
            .averageResolutionTimeInHours(averageResolutionTime)
            .reportsByType(reportsByType)
            .reportsByStatus(reportsByStatus)
            .build();
  }

  @Transactional(readOnly = true)
  public List<ReportResponseDto> getUserReports(Long userId) {
    validateUser(userId);
    List<Report> reports = reportRepository.findByReporterId(userId);
    return reports.stream().map(reportMapper::toReportResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ReportResponseDto> getAllReports() {
    List<Report> reports = reportRepository.findAll();
    return reports.stream().map(reportMapper::toReportResponse).collect(Collectors.toList());
  }

  public byte[] exportStatistics(String format, LocalDateTime startDate, LocalDateTime endDate, Long adminId) {
    ReportStatisticsDto statistics = getStatistics(startDate, endDate, null);

    switch (format.toUpperCase()) {
      case "CSV":
        return generateCsvReport(statistics);
      default:
        throw new CustomException(ErrorCode.BAD_REQUEST, "Unsupported format: " + format);
    }
  }

  private byte[] generateCsvReport(ReportStatisticsDto statistics) {
    StringBuilder csv = new StringBuilder();
    csv.append("Report Statistics\n\n");
    csv.append("Total Reports,").append(statistics.getTotalReports()).append("\n");
    csv.append("Pending Reports,").append(statistics.getPendingReports()).append("\n");
    csv.append("Resolved Reports,").append(statistics.getResolvedReports()).append("\n");
    csv.append("Rejected Reports,").append(statistics.getRejectedReports()).append("\n");
    csv.append("Average Resolution Time (hours),").append(statistics.getAverageResolutionTimeInHours()).append("\n\n");

    csv.append("Reports by Type\n");
    statistics.getReportsByType().forEach((type, count) ->
            csv.append(type).append(",").append(count).append("\n"));

    csv.append("\nReports by Status\n");
    statistics.getReportsByStatus().forEach((status, count) ->
            csv.append(status).append(",").append(count).append("\n"));

    return csv.toString().getBytes(StandardCharsets.UTF_8);
  }
}
