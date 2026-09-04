package com.mralmostcool.tarbook.core.internal.service;

import com.mralmostcool.tarbook.core.internal.domain.AppUser;
import com.mralmostcool.tarbook.core.internal.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserInternalService {

    private final AppUserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<AppUser> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public AppUser save(AppUser user) {
        OffsetDateTime now = OffsetDateTime.now();
        if (user.getCreatedAtUtc() == null) {
            user.setCreatedAtUtc(now);
        }
        user.setUpdatedAtUtc(now);
        return userRepository.save(user);
    }
}
