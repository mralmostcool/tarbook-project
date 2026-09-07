package com.mralmostcool.tarbook.seaservice.internal.repository;

import com.mralmostcool.tarbook.seaservice.internal.domain.EndorsementType;
import com.mralmostcool.tarbook.seaservice.internal.domain.SeaServiceEndorsement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeaServiceEndorsementRepository extends JpaRepository<SeaServiceEndorsement, UUID> {

    List<SeaServiceEndorsement> findBySeaServiceRecordId(UUID seaServiceId);

    Optional<SeaServiceEndorsement> findBySeaServiceRecordIdAndEndorsementType(UUID seaServiceId, EndorsementType endorsementType);
}
