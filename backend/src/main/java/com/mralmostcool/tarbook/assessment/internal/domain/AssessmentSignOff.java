package com.mralmostcool.tarbook.assessment.internal.domain;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assessment_signoffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentSignOff {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_assessment_id", nullable = false)
    private TaskAssessment taskAssessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signer_user_id", nullable = false)
    private AppUser signerUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "signer_role", nullable = false, length = 50)
    private SignOffRole signerRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SignOffVerdict verdict;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "signed_at_utc", nullable = false, updatable = false)
    private OffsetDateTime signedAtUtc;
}
