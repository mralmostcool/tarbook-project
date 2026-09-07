package com.mralmostcool.tarbook.assessment.internal.domain;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.program.internal.domain.SyllabusTask;
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
@Table(name = "task_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssessment {

    @Id
    private UUID id;

    @Column(name = "tar_book_id", nullable = false)
    private UUID tarBookId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_definition_id", nullable = false)
    private SyllabusTask taskDefinition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_user_id", nullable = false)
    private AppUser candidateUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssessmentGrade grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssessmentStatus status;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "created_at_utc", nullable = false, updatable = false)
    private OffsetDateTime createdAtUtc;

    @Column(name = "updated_at_utc", nullable = false)
    private OffsetDateTime updatedAtUtc;
}
