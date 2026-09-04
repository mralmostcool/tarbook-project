package com.mralmostcool.tarbook.core;

import com.mralmostcool.tarbook.TestcontainersConfiguration;
import com.mralmostcool.tarbook.core.dto.OrganizationDto;
import com.mralmostcool.tarbook.core.internal.domain.Organization;
import com.mralmostcool.tarbook.core.internal.domain.OrganizationType;
import com.mralmostcool.tarbook.core.internal.service.OrganizationInternalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CoreDomainIntegrationTest {

    @Autowired
    private OrganizationInternalService organizationInternalService;

    @Autowired
    private CoreService coreService;

    @Test
    void shouldSaveAndRetrieveOrganization() {
        UUID orgId = UUID.randomUUID();
        Organization org = Organization.builder()
                .id(orgId)
                .name("Anglo-Eastern Maritime Academy")
                .type(OrganizationType.MTI)
                .code("MTI-AEMA-01")
                .licenseNumber("MTI-LIC-2026")
                .build();

        organizationInternalService.save(org);

        Optional<OrganizationDto> retrieved = coreService.getOrganizationById(orgId);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Anglo-Eastern Maritime Academy");
        assertThat(retrieved.get().getType()).isEqualTo(OrganizationType.MTI);
    }
}
