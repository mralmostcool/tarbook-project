package com.mralmostcool.tarbook.core.dto;

import com.mralmostcool.tarbook.core.internal.domain.OrganizationType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class OrganizationDto {
    private UUID id;
    private String name;
    private OrganizationType type;
    private String code;
    private String licenseNumber;
    private OffsetDateTime createdAtUtc;
    private OffsetDateTime updatedAtUtc;
}
