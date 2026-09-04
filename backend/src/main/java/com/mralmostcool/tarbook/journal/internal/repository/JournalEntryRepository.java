package com.mralmostcool.tarbook.journal.internal.repository;

import com.mralmostcool.tarbook.journal.internal.domain.JournalEntry;
import com.mralmostcool.tarbook.journal.internal.domain.JournalEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    List<JournalEntry> findByCadetUserId(UUID cadetUserId);
    List<JournalEntry> findByCadetUserIdAndStatus(UUID cadetUserId, JournalEntryStatus status);
}
