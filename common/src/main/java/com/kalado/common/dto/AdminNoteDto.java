package com.kalado.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminNoteDto {
    private Long id;
    private String content;
    private Long adminId;
    private LocalDateTime createdAt;
}
