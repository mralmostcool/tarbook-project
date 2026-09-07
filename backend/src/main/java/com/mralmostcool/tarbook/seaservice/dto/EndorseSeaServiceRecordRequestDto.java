package com.mralmostcool.tarbook.seaservice.dto;

import com.mralmostcool.tarbook.seaservice.internal.domain.EndorserRole;
import com.mralmostcool.tarbook.seaservice.internal.domain.EndorsementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndorseSeaServiceRecordRequestDto {
    private UUID endorserUserId;
    private EndorserRole endorserRole;
    private EndorsementType endorsementType;
    private String conductRating;
    private String abilityRating;
    private String comments;
    private String keyId;
    private byte[] signatureBytes;
}
