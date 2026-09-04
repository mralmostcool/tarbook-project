package com.mralmostcool.tarbook.program.internal.repository;

import com.mralmostcool.tarbook.program.internal.domain.StcwProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StcwProgramRepository extends JpaRepository<StcwProgram, UUID> {
    Optional<StcwProgram> findByCode(String code);
}
