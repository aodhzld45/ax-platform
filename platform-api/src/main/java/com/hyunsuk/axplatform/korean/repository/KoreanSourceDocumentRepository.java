package com.hyunsuk.axplatform.korean.repository;

import com.hyunsuk.axplatform.korean.entity.KoreanSourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KoreanSourceDocumentRepository
        extends JpaRepository<KoreanSourceDocument, Long> {

    Optional<KoreanSourceDocument> findByDocumentId(Long documentId);
}
