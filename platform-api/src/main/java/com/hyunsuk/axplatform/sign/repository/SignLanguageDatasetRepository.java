package com.hyunsuk.axplatform.sign.repository;

import com.hyunsuk.axplatform.sign.entity.SignLanguageDataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignLanguageDatasetRepository
        extends JpaRepository<SignLanguageDataset, Long> {

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Page<SignLanguageDataset> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Optional<SignLanguageDataset> findWithDocumentById(Long id);

    Optional<SignLanguageDataset> findByDocumentId(Long documentId);

    boolean existsByDocumentId(Long documentId);
}
