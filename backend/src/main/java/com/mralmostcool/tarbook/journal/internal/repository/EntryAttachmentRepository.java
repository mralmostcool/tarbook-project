package com.mralmostcool.tarbook.journal.internal.repository;

import com.mralmostcool.tarbook.journal.internal.domain.EntryAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EntryAttachmentRepository extends JpaRepository<EntryAttachment, UUID> {
    List<EntryAttachment> findByJournalEntryId(UUID journalEntryId);
}
