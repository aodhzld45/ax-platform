package com.hyunsuk.axplatform.sign.repository;

import com.hyunsuk.axplatform.sign.entity.SignLanguageDataset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignLanguageDatasetRepository
        extends JpaRepository<SignLanguageDataset, Long> {

    Optional<SignLanguageDataset> findByDocumentId(Long documentId);

    boolean existsByDocumentId(Long documentId);
}
