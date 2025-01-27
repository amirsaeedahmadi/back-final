package com.kalado.gateway.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.dto.*;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.report.ReportApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
  private final ReportApi reportApi;
  private final ObjectMapper objectMapper;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Authentication(userId = "#userId")
  public ReportResponseDto createReport(
          Long userId,
          @RequestPart("report") String reportJson,
          @RequestPart(value = "evidence", required = false) List<MultipartFile> evidenceFiles) {
    try {
      log.debug("Creating report with data: {} and {} evidence files", reportJson,
              evidenceFiles != null ? evidenceFiles.size() : 0);
      return reportApi.createReport(reportJson, evidenceFiles, userId);
    } catch (Exception e) {
      log.error("Error creating report: {}", e.getMessage());
      throw new CustomException(ErrorCode.BAD_REQUEST, "Error processing request: " + e.getMessage());
    }
  }

  @GetMapping("/evidence/{filename}")
  public Resource getEvidence(@PathVariable String filename) {
    return reportApi.getEvidence(filename);
  }

  @GetMapping("/my-reports")
  @Authentication(userId = "#userId")
  public List<ReportResponseDto> getMyReports(Long userId) {
    return reportApi.getUserReports(userId);
  }

  @GetMapping("/my-reports/{userId}")
  public List<ReportResponseDto> getUserReportsByUserId(@PathVariable Long userId) {
    return reportApi.getUserReports(userId);
  }

  @GetMapping("/admin/all")
  @Authentication(userId = "#userId")
  public List<ReportResponseDto> getAllReports(
          @RequestParam(required = false) String status,
          Long userId) {
    return reportApi.getAllReports(status, userId);
  }

  @PostMapping("/admin/status/{reportId}")
  @Authentication(userId = "#userId")
  public ReportResponseDto updateReportStatus(
          @PathVariable Long reportId,
          @RequestBody ReportStatusUpdateDto request,
          Long userId) {
    return reportApi.updateReportStatus(reportId, request, userId);
  }

  @GetMapping("/admin/statistics")
  @Authentication(userId = "#userId")
  public ReportStatisticsDto getStatistics(
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
          @RequestParam(required = false) String violationType,
          Long userId) {
    return reportApi.getStatistics(startDate, endDate, violationType, userId);
  }

  @GetMapping("/admin/statistics/export")
  @Authentication(userId = "#userId")
  public byte[] exportStatistics(
          @RequestParam String format,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
          Long userId) {
    return reportApi.exportStatistics(format, startDate, endDate, userId);
  }
}