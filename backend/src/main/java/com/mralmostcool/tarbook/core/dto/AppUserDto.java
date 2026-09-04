package com.mralmostcool.tarbook.core.dto;

import com.mralmostcool.tarbook.core.internal.domain.SystemRole;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class AppUserDto {
    private UUID id;
    private String email;
    private String fullName;
    private SystemRole systemRole;
    private OffsetDateTime createdAtUtc;
    private OffsetDateTime updatedAtUtc;
}
