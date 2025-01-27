package com.kalado.reporting.domain.model;

import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.ReportResponseDto;
import com.kalado.common.enums.ReportStatus;
import org.mapstruct.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReportMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reporterId", source = "reporterId")
    @Mapping(target = "reportedUserId", ignore = true)
    @Mapping(target = "evidenceFiles", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastUpdatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", constant = "SUBMITTED")
    @Mapping(target = "adminId", ignore = true)
    @Mapping(target = "adminNotes", ignore = true)
    @Mapping(target = "userBlocked", constant = "false")
    Report toReport(ReportCreateRequestDto reportDto, Long reporterId);

    @Named("evidenceFilesToUrls")
    default List<String> evidenceFilesToUrls(List<MultipartFile> files) {
        return Collections.emptyList();
    }

    @Mapping(target = "evidenceFiles", source = "evidenceFiles")
    ReportResponseDto toReportResponse(Report report);
}