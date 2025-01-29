package com.kalado.reporting;

import com.kalado.common.dto.*;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.ProductStatus;
import com.kalado.common.enums.ReportStatus;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.common.feign.user.UserApi;
import com.kalado.reporting.application.service.EmailService;
import com.kalado.reporting.application.service.EvidenceService;
import com.kalado.reporting.application.service.ReportService;
import com.kalado.reporting.domain.model.Report;
import com.kalado.reporting.domain.model.ReportMapper;
import com.kalado.reporting.domain.model.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private UserApi userApi;
    @Mock
    private ProductApi productApi;
    @Mock
    private EmailService emailService;
    @Mock
    private EvidenceService evidenceService;
    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportService reportService;

    @Captor
    private ArgumentCaptor<Report> reportCaptor;

    private ReportCreateRequestDto validRequest;
    private static final Long REPORTER_ID = 1L;
    private static final Long REPORTED_CONTENT_ID = 2L;
    private static final Long REPORTED_USER_ID = 3L;

    @BeforeEach
    void setUp() {
        validRequest = ReportCreateRequestDto.builder()
                .violationType("INAPPROPRIATE_CONTENT")
                .description("Test description")
                .reportedContentId(REPORTED_CONTENT_ID)
                .evidenceFiles(Collections.emptyList())
                .build();
    }

    @Test
    void createReport_Success() {
        ProductDto validProduct = ProductDto.builder()
                .id(REPORTED_CONTENT_ID)
                .sellerId(REPORTED_USER_ID)
                .build();

        Report validReport = Report.builder()
                .id(1L)
                .reporterId(REPORTER_ID)
                .reportedUserId(REPORTED_USER_ID)
                .status(ReportStatus.SUBMITTED)
                .build();

        when(userApi.getUserProfile(any())).thenReturn(UserDto.builder()
                .id(REPORTER_ID)
                .username("test@example.com")
                .build());
        when(productApi.getProduct(REPORTED_CONTENT_ID)).thenReturn(validProduct);
        when(reportMapper.toReport(any(), eq(REPORTER_ID))).thenReturn(validReport);
        when(reportRepository.save(any(Report.class))).thenReturn(validReport);
        when(reportMapper.toReportResponse(any())).thenReturn(ReportResponseDto.builder()
                .id(1L)
                .status(ReportStatus.SUBMITTED)
                .build());

        ReportResponseDto response = reportService.createReport(validRequest, REPORTER_ID);

        assertNotNull(response);
        verify(reportRepository).save(reportCaptor.capture());
        verify(emailService).sendReportConfirmation(REPORTER_ID);

        Report savedReport = reportCaptor.getValue();
        assertEquals(REPORTER_ID, savedReport.getReporterId());
        assertEquals(REPORTED_USER_ID, savedReport.getReportedUserId());

        verify(userApi, times(2)).getUserProfile(any());
        verify(productApi).getProduct(REPORTED_CONTENT_ID);
        verify(reportMapper).toReport(any(), eq(REPORTER_ID));
        verify(reportMapper).toReportResponse(any());
    }

    @Test
    void updateReportStatus_Success() {
        Report existingReport = Report.builder()
                .id(1L)
                .reporterId(REPORTER_ID)
                .reportedUserId(REPORTED_USER_ID)
                .status(ReportStatus.SUBMITTED)
                .build();

        when(reportRepository.findById(1L)).thenReturn(Optional.of(existingReport));
        when(reportRepository.save(any(Report.class))).thenReturn(existingReport);
        when(reportMapper.toReportResponse(any())).thenReturn(ReportResponseDto.builder()
                .id(1L)
                .status(ReportStatus.RESOLVED)
                .build());

        ReportStatusUpdateDto updateRequest = ReportStatusUpdateDto.builder()
                .status(ReportStatus.RESOLVED)
                .adminNotes("Test notes")
                .build();

        ReportResponseDto response = reportService.updateReportStatus(1L, updateRequest, 1L);

        assertNotNull(response);
        verify(reportRepository).save(reportCaptor.capture());
        Report savedReport = reportCaptor.getValue();
        assertEquals(ReportStatus.RESOLVED, savedReport.getStatus());
        assertEquals("Test notes", savedReport.getAdminNotes());
    }

    @Test
    void updateReportStatus_WithUserBlock() {
        Report existingReport = Report.builder()
                .id(1L)
                .reporterId(REPORTER_ID)
                .reportedUserId(REPORTED_USER_ID)
                .status(ReportStatus.SUBMITTED)
                .build();

        when(reportRepository.findById(1L)).thenReturn(Optional.of(existingReport));
        when(reportRepository.save(any())).thenReturn(existingReport);
        when(reportMapper.toReportResponse(any())).thenReturn(new ReportResponseDto());

        ReportStatusUpdateDto updateRequest = ReportStatusUpdateDto.builder()
                .status(ReportStatus.RESOLVED)
                .blockUser(true)
                .build();

        reportService.updateReportStatus(1L, updateRequest, 1L);

        verify(userApi).blockUser(REPORTED_USER_ID);
    }

    @Test
    void getUserReports_Success() {
        when(userApi.getUserProfile(REPORTER_ID)).thenReturn(UserDto.builder().id(REPORTER_ID).build());
        when(reportRepository.findByReporterId(REPORTER_ID))
                .thenReturn(Arrays.asList(
                        Report.builder().id(1L).build(),
                        Report.builder().id(2L).build()
                ));
        when(reportMapper.toReportResponse(any())).thenReturn(new ReportResponseDto());

        List<ReportResponseDto> reports = reportService.getUserReports(REPORTER_ID);

        assertNotNull(reports);
        assertEquals(2, reports.size());
        verify(reportMapper, times(2)).toReportResponse(any());
    }

    @Test
    void getUserReports_UserNotFound() {
        when(userApi.getUserProfile(REPORTER_ID)).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class,
                () -> reportService.getUserReports(REPORTER_ID));
        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getStatistics_Success() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Report> reports = Arrays.asList(
                Report.builder()
                        .status(ReportStatus.RESOLVED)
                        .violationType("TYPE_1")
                        .createdAt(startDate.plusDays(1))
                        .lastUpdatedAt(startDate.plusDays(2))
                        .build(),
                Report.builder()
                        .status(ReportStatus.SUBMITTED)
                        .violationType("TYPE_2")
                        .createdAt(startDate.plusDays(3))
                        .build()
        );

        when(reportRepository.findByCreatedAtBetween(any(), any())).thenReturn(reports);

        ReportStatisticsDto stats = reportService.getStatistics(startDate, endDate, null);

        assertNotNull(stats);
        assertEquals(2, stats.getTotalReports());
        assertEquals(1, stats.getPendingReports());
        assertEquals(1, stats.getResolvedReports());
    }
}