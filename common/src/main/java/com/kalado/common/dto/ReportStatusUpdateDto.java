package com.kalado.common.dto;

import com.kalado.common.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatusUpdateDto {
  private ReportStatus status;
  private String adminNotes;
  private boolean blockUser;
  private boolean blockProduct;
  private String blockReason;
}