package com.hyunsuk.axplatform.korean.repository;

import com.hyunsuk.axplatform.korean.entity.KoreanSourceDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KoreanSourceDocumentRepository
        extends JpaRepository<KoreanSourceDocument, Long> {

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Page<KoreanSourceDocument> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Optional<KoreanSourceDocument> findWithDocumentById(Long id);

    Optional<KoreanSourceDocument> findByDocumentId(Long documentId);

    boolean existsByDocumentId(Long documentId);
}
