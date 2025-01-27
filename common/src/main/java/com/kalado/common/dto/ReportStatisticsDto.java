package com.kalado.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatisticsDto {
    private long totalReports;
    private long pendingReports;
    private long resolvedReports;
    private long rejectedReports;
    private double averageResolutionTimeInHours;
    private Map<String, Long> reportsByType;
    private Map<String, Long> reportsByStatus;
}