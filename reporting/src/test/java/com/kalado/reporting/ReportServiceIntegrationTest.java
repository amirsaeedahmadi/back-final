//package com.kalado.reporting;
//
//import com.kalado.common.dto.*;
//import com.kalado.common.enums.ProductStatus;
//import com.kalado.common.enums.ReportStatus;
//import com.kalado.common.exception.CustomException;
//import com.kalado.common.feign.product.ProductApi;
//import com.kalado.common.feign.user.UserApi;
//import com.kalado.reporting.application.service.EmailService;
//import com.kalado.reporting.application.service.EvidenceService;
//import com.kalado.reporting.application.service.ReportService;
//import com.kalado.reporting.domain.model.Report;
//import com.kalado.reporting.domain.model.ReportMapper;
//import com.kalado.reporting.domain.model.ReportRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.annotation.DirtiesContext;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class ReportServiceIntegrationTest {
//
//    @Autowired
//    private ReportService reportService;
//
//    @Autowired
//    private ReportRepository reportRepository;
//
//    @MockBean
//    private UserApi userApi;
//
//    @MockBean
//    private ProductApi productApi;
//
//    @MockBean
//    private EmailService emailService;
//
//    @MockBean
//    private EvidenceService evidenceService;
//
//    @MockBean
//    private ReportMapper reportMapper;
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    private ReportCreateRequestDto validRequest;
//    private static final Long REPORTER_ID = 1L;
//    private static final Long REPORTED_CONTENT_ID = 2L;
//    private static final Long REPORTED_USER_ID = 3L;
//
//    @BeforeEach
//    void setUp() {
//        // Clean database
//        reportRepository.deleteAll();
//        entityManager.flush();
//        entityManager.clear();
//
//        // Set up valid request
//        validRequest = ReportCreateRequestDto.builder()
//                .violationType("INAPPROPRIATE_CONTENT")
//                .description("Test description")
//                .reportedContentId(REPORTED_CONTENT_ID)
//                .evidenceFiles(Collections.emptyList())
//                .build();
//
//        // Mock UserApi behavior
//        UserDto mockUser = UserDto.builder()
//                .id(REPORTER_ID)
//                .username("test@example.com")
//                .blocked(false)
//                .build();
//        when(userApi.getUserProfile(REPORTER_ID)).thenReturn(mockUser);
//        when(userApi.getUserProfile(REPORTED_USER_ID)).thenReturn(UserDto.builder()
//                .id(REPORTED_USER_ID)
//                .build());
//
//        // Mock ProductApi behavior
//        ProductDto mockProduct = ProductDto.builder()
//                .id(REPORTED_CONTENT_ID)
//                .sellerId(REPORTED_USER_ID)
//                .build();
//        when(productApi.getProduct(REPORTED_CONTENT_ID)).thenReturn(mockProduct);
//
//        // Mock Report Mapper behavior
//        when(reportMapper.toReport(any(ReportCreateRequestDto.class), eq(REPORTER_ID)))
//                .thenAnswer(invocation -> {
//                    ReportCreateRequestDto req = invocation.getArgument(0);
//                    return Report.builder()
//                            .violationType(req.getViolationType())
//                            .description(req.getDescription())
//                            .reporterId(REPORTER_ID)
//                            .reportedUserId(REPORTED_USER_ID)
//                            .reportedContentId(req.getReportedContentId())
//                            .status(ReportStatus.SUBMITTED)
//                            .createdAt(LocalDateTime.now())
//                            .lastUpdatedAt(LocalDateTime.now())
//                            .build();
//                });
//
//        when(reportMapper.toReportResponse(any(Report.class)))
//                .thenAnswer(invocation -> {
//                    Report report = invocation.getArgument(0);
//                    return ReportResponseDto.builder()
//                            .id(report.getId())
//                            .violationType(report.getViolationType())
//                            .description(report.getDescription())
//                            .reporterId(report.getReporterId())
//                            .reportedContentId(report.getReportedContentId())
//                            .status(report.getStatus())
//                            .build();
//                });
//
//        // Mock EmailService behavior
//        doNothing().when(emailService).sendReportConfirmation(anyLong());
//    }
//
//    @Test
//    void createAndUpdateReportFlow() {
//        // Create report
//        ReportResponseDto createdReport = reportService.createReport(validRequest, REPORTER_ID);
//        assertNotNull(createdReport);
//
//        // Verify report was created in database
//        Report savedReport = reportRepository.findById(createdReport.getId()).orElseThrow();
//        assertEquals(REPORTER_ID, savedReport.getReporterId());
//        assertEquals(REPORTED_USER_ID, savedReport.getReportedUserId());
//        assertEquals(ReportStatus.SUBMITTED, savedReport.getStatus());
//
//        // Update report status
//        ReportStatusUpdateDto updateRequest = ReportStatusUpdateDto.builder()
//                .status(ReportStatus.RESOLVED)
//                .adminNotes("Report resolved")
//                .blockUser(true)
//                .build();
//
//        ReportResponseDto updatedReport = reportService.updateReportStatus(
//                savedReport.getId(),
//                updateRequest,
//                1L
//        );
//
//        assertNotNull(updatedReport);
//        assertEquals(ReportStatus.RESOLVED, updatedReport.getStatus());
//        verify(userApi).blockUser(REPORTED_USER_ID);
//    }
//
//    @Test
//    void getUserReports() {
//        // Create test reports
//        Report report1 = createTestReport(REPORTER_ID, ReportStatus.SUBMITTED);
//        Report report2 = createTestReport(REPORTER_ID, ReportStatus.RESOLVED);
//        Report report3 = createTestReport(999L, ReportStatus.SUBMITTED);
//
//        // Mock mapper for report responses
//        when(reportMapper.toReportResponse(any(Report.class)))
//                .thenAnswer(invocation -> {
//                    Report report = invocation.getArgument(0);
//                    return ReportResponseDto.builder()
//                            .id(report.getId())
//                            .reporterId(report.getReporterId())
//                            .status(report.getStatus())
//                            .build();
//                });
//
//        List<ReportResponseDto> reports = reportService.getUserReports(REPORTER_ID);
//
//        assertNotNull(reports);
//        assertEquals(2, reports.size());
//        assertTrue(reports.stream().allMatch(r -> r.getReporterId().equals(REPORTER_ID)));
//    }
//
//    private Report createTestReport(Long reporterId, ReportStatus status) {
//        Report report = Report.builder()
//                .reporterId(reporterId)
//                .reportedUserId(REPORTED_USER_ID)
//                .reportedContentId(REPORTED_CONTENT_ID)
//                .violationType("TEST_TYPE")
//                .description("Test description")
//                .status(status)
//                .createdAt(LocalDateTime.now())
//                .lastUpdatedAt(LocalDateTime.now())
//                .build();
//        return reportRepository.save(report);
//    }
//}