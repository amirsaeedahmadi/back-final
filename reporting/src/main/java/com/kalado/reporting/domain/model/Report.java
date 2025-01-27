package com.kalado.reporting.domain.model;

import com.kalado.common.enums.ReportStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String violationType;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Long reporterId;

    @Column(nullable = false)
    private Long reportedUserId;

    private Long reportedContentId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_evidence_files", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "file_url")
    @Builder.Default
    private List<String> evidenceFiles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, columnDefinition = "varchar(32) default 'SUBMITTED'")
    private ReportStatus status;

    private String adminNotes;

    private Long adminId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean userBlocked = false;

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}