package com.mralmostcool.tarbook.journal.internal.service;

import com.mralmostcool.tarbook.journal.internal.domain.EvidenceArtifact;
import com.mralmostcool.tarbook.journal.internal.repository.EvidenceArtifactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvidenceStorageInternalService {

    private final EvidenceArtifactRepository artifactRepository;

    public String computeSha256Hash(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Artifact data cannot be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    @Transactional
    public EvidenceArtifact storeArtifact(EvidenceArtifact artifact) {
        return artifactRepository.save(artifact);
    }

    @Transactional(readOnly = true)
    public List<EvidenceArtifact> findByJournalEntryId(UUID journalEntryId) {
        return artifactRepository.findByJournalEntryId(journalEntryId);
    }

    @Transactional(readOnly = true)
    public boolean verifyArtifactHash(UUID artifactId, byte[] rawContent) {
        Optional<EvidenceArtifact> artifactOpt = artifactRepository.findById(artifactId);
        if (artifactOpt.isEmpty()) {
            return false;
        }
        String calculatedHash = computeSha256Hash(rawContent);
        return calculatedHash.equalsIgnoreCase(artifactOpt.get().getContentHash());
    }
}
