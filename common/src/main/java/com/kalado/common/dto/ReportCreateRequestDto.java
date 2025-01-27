package com.kalado.common.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequestDto {
  private String violationType;
  private String description;
  private Long reportedContentId;
  private List<MultipartFile> evidenceFiles;
}
