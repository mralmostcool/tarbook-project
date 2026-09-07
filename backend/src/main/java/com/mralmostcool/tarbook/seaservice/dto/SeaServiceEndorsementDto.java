package com.mralmostcool.tarbook.seaservice.dto;

import com.mralmostcool.tarbook.seaservice.internal.domain.EndorserRole;
import com.mralmostcool.tarbook.seaservice.internal.domain.EndorsementType;
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
public class SeaServiceEndorsementDto {
    private UUID id;
    private UUID seaServiceId;
    private UUID endorserUserId;
    private String endorserName;
    private EndorserRole endorserRole;
    private EndorsementType endorsementType;
    private String conductRating;
    private String abilityRating;
    private String comments;
    private String keyId;
    private String signaturePayloadHash;
    private OffsetDateTime signedAtUtc;
}
