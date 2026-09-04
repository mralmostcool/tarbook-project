package com.mralmostcool.tarbook.core;

import com.mralmostcool.tarbook.core.dto.AppUserDto;
import com.mralmostcool.tarbook.core.dto.CandidateDto;
import com.mralmostcool.tarbook.core.dto.OrganizationDto;
import com.mralmostcool.tarbook.core.internal.domain.Organization;
import com.mralmostcool.tarbook.core.internal.service.AppUserInternalService;
import com.mralmostcool.tarbook.core.internal.service.CandidateInternalService;
import com.mralmostcool.tarbook.core.internal.service.OrganizationInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoreService {

    private final OrganizationInternalService organizationInternalService;
    private final AppUserInternalService userInternalService;
    private final CandidateInternalService candidateInternalService;

    @Transactional(readOnly = true)
    public List<OrganizationDto> getAllOrganizations() {
        return organizationInternalService.findAll().stream()
                .map(this::mapToOrganizationDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<OrganizationDto> getOrganizationById(UUID id) {
        return organizationInternalService.findById(id).map(this::mapToOrganizationDto);
    }

    @Transactional(readOnly = true)
    public Optional<AppUserDto> getUserById(UUID id) {
        return userInternalService.findById(id).map(user -> AppUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .systemRole(user.getSystemRole())
                .createdAtUtc(user.getCreatedAtUtc())
                .updatedAtUtc(user.getUpdatedAtUtc())
                .build());
    }

    @Transactional(readOnly = true)
    public Optional<CandidateDto> getCandidateById(UUID id) {
        return candidateInternalService.findById(id).map(candidate -> CandidateDto.builder()
                .id(candidate.getId())
                .sponsoringOrgId(candidate.getSponsoringOrganization() != null ? candidate.getSponsoringOrganization().getId() : null)
                .indosNumber(candidate.getIndosNumber())
                .cdcNumber(candidate.getCdcNumber())
                .trainingStream(candidate.getTrainingStream())
                .dateOfBirth(candidate.getDateOfBirth())
                .createdAtUtc(candidate.getCreatedAtUtc())
                .updatedAtUtc(candidate.getUpdatedAtUtc())
                .build());
    }

    private OrganizationDto mapToOrganizationDto(Organization org) {
        return OrganizationDto.builder()
                .id(org.getId())
                .name(org.getName())
                .type(org.getType())
                .code(org.getCode())
                .licenseNumber(org.getLicenseNumber())
                .createdAtUtc(org.getCreatedAtUtc())
                .updatedAtUtc(org.getUpdatedAtUtc())
                .build();
    }
}
