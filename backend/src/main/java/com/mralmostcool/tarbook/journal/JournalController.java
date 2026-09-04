package com.mralmostcool.tarbook.journal;

import com.mralmostcool.tarbook.journal.dto.AuditLogDto;
import com.mralmostcool.tarbook.journal.dto.CreateJournalEntryRequestDto;
import com.mralmostcool.tarbook.journal.dto.JournalEntryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journal-entries")
@RequiredArgsConstructor
@Tag(name = "TAR Journal & Evidence", description = "Endpoints for daily cadet training entries, evidence storage, and audit logs")
public class JournalController {

    private final JournalService journalService;

    @PostMapping
    @Operation(summary = "Create daily TAR journal entry")
    public ResponseEntity<JournalEntryDto> createJournalEntry(@Valid @RequestBody CreateJournalEntryRequestDto request) {
        return ResponseEntity.ok(journalService.createJournalEntry(request));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit journal entry for officer verification")
    public ResponseEntity<JournalEntryDto> submitJournalEntry(@PathVariable UUID id) {
        return ResponseEntity.ok(journalService.submitJournalEntry(id));
    }

    @PostMapping("/{id}/verify")
    @Operation(summary = "Verify journal entry by supervising officer")
    public ResponseEntity<JournalEntryDto> verifyJournalEntry(
            @PathVariable UUID id,
            @RequestParam UUID officerUserId,
            @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(journalService.verifyJournalEntryByOfficer(id, officerUserId, comments));
    }

    @GetMapping("/cadet/{cadetUserId}")
    @Operation(summary = "Get journal entries for a cadet")
    public ResponseEntity<List<JournalEntryDto>> getCadetEntries(@PathVariable UUID cadetUserId) {
        return ResponseEntity.ok(journalService.getJournalEntriesForCadet(cadetUserId));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get immutable audit trail for an entity")
    public ResponseEntity<List<AuditLogDto>> getAuditLogs(
            @RequestParam String entityName,
            @RequestParam UUID entityId) {
        return ResponseEntity.ok(journalService.getAuditLogsForEntity(entityName, entityId));
    }
}
