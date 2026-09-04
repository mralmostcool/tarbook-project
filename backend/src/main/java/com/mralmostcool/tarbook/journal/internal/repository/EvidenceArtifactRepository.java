package com.mralmostcool.tarbook.journal.internal.repository;

import com.mralmostcool.tarbook.journal.internal.domain.EvidenceArtifact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EvidenceArtifactRepository extends JpaRepository<EvidenceArtifact, UUID> {
    List<EvidenceArtifact> findByJournalEntryId(UUID journalEntryId);
}
