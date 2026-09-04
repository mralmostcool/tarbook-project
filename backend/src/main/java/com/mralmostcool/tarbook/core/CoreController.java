package com.mralmostcool.tarbook.core;

import com.mralmostcool.tarbook.core.dto.AppUserDto;
import com.mralmostcool.tarbook.core.dto.CandidateDto;
import com.mralmostcool.tarbook.core.dto.OrganizationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/core")
@RequiredArgsConstructor
public class CoreController {

    private final CoreService coreService;

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationDto>> getOrganizations() {
        return ResponseEntity.ok(coreService.getAllOrganizations());
    }

    @GetMapping("/organizations/{id}")
    public ResponseEntity<OrganizationDto> getOrganizationById(@PathVariable UUID id) {
        return coreService.getOrganizationById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AppUserDto> getUserById(@PathVariable UUID id) {
        return coreService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/candidates/{id}")
    public ResponseEntity<CandidateDto> getCandidateById(@PathVariable UUID id) {
        return coreService.getCandidateById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
