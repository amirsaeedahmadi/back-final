package com.kalado.gateway.adapters;

import com.kalado.common.feign.report.ReportApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/evidence")
@RequiredArgsConstructor
@Slf4j
public class EvidenceController {
    private final ReportApi reportApi;

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getEvidence(@PathVariable String filename) {
        Resource evidence = reportApi.getEvidence(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(evidence);
    }
}