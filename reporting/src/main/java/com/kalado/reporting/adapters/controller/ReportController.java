package com.kalado.reporting.adapters.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.dto.*;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.report.ReportApi;
import com.kalado.reporting.application.service.EvidenceService;
import com.kalado.reporting.application.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController implements ReportApi {
    private final EvidenceService evidenceService;
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final ReportService reportService;
    private final ObjectMapper objectMapper;


    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReportResponseDto createReport(
            @RequestPart("report") String reportJson,
            @RequestPart(value = "evidence", required = false) List<MultipartFile> evidenceFiles,
            @RequestParam("userId") Long reporterId) {
        try {
            ReportCreateRequestDto request = objectMapper.readValue(reportJson, ReportCreateRequestDto.class);
            log.debug("Creating report with data: {} and {} evidence files", request,
                    evidenceFiles != null ? evidenceFiles.size() : 0);

            if (evidenceFiles != null) {
                request.setEvidenceFiles(evidenceFiles);
            }

            return reportService.createReport(request, reporterId);
        } catch (Exception e) {
            log.error("Error creating report: {}", e.getMessage());
            throw new CustomException(ErrorCode.BAD_REQUEST, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    @GetMapping(value = "/evidence/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    public Resource getEvidence(@PathVariable String filename) {
        return evidenceService.getEvidence(filename);
    }

    @Override
    public List<ReportResponseDto> getUserReports(Long userId) {
        return reportService.getUserReports(userId);
    }

    @Override
    public List<ReportResponseDto> getAllReports(String status, Long adminId) {
        return reportService.getAllReports();
    }

    @Override
    public ReportResponseDto updateReportStatus(Long reportId, ReportStatusUpdateDto request, Long adminId) {
        return reportService.updateReportStatus(reportId, request, adminId);
    }

    @Override
    public ReportStatisticsDto getStatistics(LocalDateTime startDate, LocalDateTime endDate, String violationType, Long adminId) {
        return reportService.getStatistics(startDate, endDate, violationType);
    }

    @Override
    public byte[] exportStatistics(String format, LocalDateTime startDate, LocalDateTime endDate, Long adminId) {
        return reportService.exportStatistics(format, startDate, endDate, adminId);
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        return switch (extension) {
            case ".pdf" -> "application/pdf";
            case ".txt" -> "text/plain";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            default -> "application/octet-stream";
        };
    }
}