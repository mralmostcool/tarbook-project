package com.mralmostcool.tarbook.core.internal.repository;

import com.mralmostcool.tarbook.core.internal.domain.VesselCrewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VesselCrewAssignmentRepository extends JpaRepository<VesselCrewAssignment, UUID> {
    List<VesselCrewAssignment> findByOfficerUserId(UUID officerUserId);
    List<VesselCrewAssignment> findByVesselImo(String vesselImo);
    Optional<VesselCrewAssignment> findBySponsoringOrganizationIdAndExternalAssignmentId(UUID sponsoringOrgId, String externalAssignmentId);
}
