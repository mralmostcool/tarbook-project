package com.mralmostcool.tarbook.journal;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.journal.dto.AuditLogDto;
import com.mralmostcool.tarbook.journal.dto.CreateJournalEntryRequestDto;
import com.mralmostcool.tarbook.journal.dto.JournalEntryDto;
import com.mralmostcool.tarbook.journal.internal.domain.AuditLog;
import com.mralmostcool.tarbook.journal.internal.domain.JournalEntry;
import com.mralmostcool.tarbook.journal.internal.domain.JournalEntryStatus;
import com.mralmostcool.tarbook.journal.internal.repository.JournalEntryRepository;
import com.mralmostcool.tarbook.journal.internal.service.AuditLogInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalEntryRepository journalEntryRepository;
    private final AppUserInternalService userInternalService;
    private final AuditLogInternalService auditLogInternalService;

    @Transactional
    public JournalEntryDto createJournalEntry(CreateJournalEntryRequestDto request) {
        AppUser cadet = userInternalService.findById(request.getCadetUserId())
                .orElseThrow(() -> new IllegalArgumentException("Cadet user not found: " + request.getCadetUserId()));

        OffsetDateTime now = OffsetDateTime.now();
        JournalEntry entry = JournalEntry.builder()
                .id(UUID.randomUUID())
                .cadetUser(cadet)
                .entryDate(request.getEntryDate())
                .seaDaysLogged(request.getSeaDaysLogged())
                .watchkeepingHours(request.getWatchkeepingHours())
                .status(JournalEntryStatus.DRAFT)
                .cadetComments(request.getCadetComments())
                .createdAtUtc(now)
                .updatedAtUtc(now)
                .build();

        JournalEntry saved = journalEntryRepository.save(entry);
        auditLogInternalService.logAction(cadet, "CREATE_JOURNAL_ENTRY", "JournalEntry", saved.getId(), "{\"status\":\"DRAFT\"}");

        return mapToDto(saved);
    }

    @Transactional
    public JournalEntryDto submitJournalEntry(UUID entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found: " + entryId));

        entry.setStatus(JournalEntryStatus.SUBMITTED);
        entry.setUpdatedAtUtc(OffsetDateTime.now());
        JournalEntry updated = journalEntryRepository.save(entry);

        auditLogInternalService.logAction(entry.getCadetUser(), "SUBMIT_JOURNAL_ENTRY", "JournalEntry", entryId, "{\"status\":\"SUBMITTED\"}");
        return mapToDto(updated);
    }

    @Transactional
    public JournalEntryDto verifyJournalEntryByOfficer(UUID entryId, UUID officerUserId, String comments) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found: " + entryId));

        AppUser officer = userInternalService.findById(officerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Officer user not found: " + officerUserId));

        entry.setStatus(JournalEntryStatus.VERIFIED);
        entry.setSupervisorComments(comments);
        entry.setUpdatedAtUtc(OffsetDateTime.now());

        JournalEntry updated = journalEntryRepository.save(entry);
        auditLogInternalService.logAction(officer, "VERIFY_JOURNAL_ENTRY", "JournalEntry", entryId, "{\"status\":\"VERIFIED\"}");

        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<JournalEntryDto> getJournalEntriesForCadet(UUID cadetUserId) {
        return journalEntryRepository.findByCadetUserId(cadetUserId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<JournalEntryDto> getJournalEntryById(UUID entryId) {
        return journalEntryRepository.findById(entryId).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getAuditLogsForEntity(String entityName, UUID entityId) {
        return auditLogInternalService.findLogsByEntity(entityName, entityId).stream()
                .map(this::mapAuditLogToDto)
                .collect(Collectors.toList());
    }

    private JournalEntryDto mapToDto(JournalEntry entry) {
        return JournalEntryDto.builder()
                .id(entry.getId())
                .cadetUserId(entry.getCadetUser().getId())
                .vesselAssignmentId(entry.getVesselAssignment() != null ? entry.getVesselAssignment().getId() : null)
                .entryDate(entry.getEntryDate())
                .seaDaysLogged(entry.getSeaDaysLogged())
                .watchkeepingHours(entry.getWatchkeepingHours())
                .status(entry.getStatus())
                .cadetComments(entry.getCadetComments())
                .supervisorComments(entry.getSupervisorComments())
                .createdAtUtc(entry.getCreatedAtUtc())
                .build();
    }

    private AuditLogDto mapAuditLogToDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .actorUserId(log.getActorUser().getId())
                .action(log.getAction())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .detailsJson(log.getDetailsJson())
                .prevHash(log.getPrevHash())
                .entryHash(log.getEntryHash())
                .loggedAtUtc(log.getLoggedAtUtc())
                .build();
    }
}
