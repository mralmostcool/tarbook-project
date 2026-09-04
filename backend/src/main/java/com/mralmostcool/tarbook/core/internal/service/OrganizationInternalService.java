package com.mralmostcool.tarbook.core.internal.service;

import com.mralmostcool.tarbook.core.internal.domain.Organization;
import com.mralmostcool.tarbook.core.internal.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationInternalService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public Optional<Organization> findById(UUID id) {
        return organizationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Organization> findByCode(String code) {
        return organizationRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    @Transactional
    public Organization save(Organization organization) {
        OffsetDateTime now = OffsetDateTime.now();
        if (organization.getCreatedAtUtc() == null) {
            organization.setCreatedAtUtc(now);
        }
        organization.setUpdatedAtUtc(now);
        return organizationRepository.save(organization);
    }
}
