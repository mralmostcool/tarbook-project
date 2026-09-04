package com.mralmostcool.tarbook.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private UUID id;
    private UUID actorUserId;
    private String action;
    private String entityName;
    private UUID entityId;
    private String detailsJson;
    private String prevHash;
    private String entryHash;
    private OffsetDateTime loggedAtUtc;
}
