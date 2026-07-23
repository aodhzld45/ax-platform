package com.hyunsuk.axplatform.museum.repository;

import com.hyunsuk.axplatform.museum.entity.MuseumManual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MuseumManualRepository
        extends JpaRepository<MuseumManual, Long> {

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Page<MuseumManual> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Optional<MuseumManual> findWithDocumentById(Long id);

    Optional<MuseumManual> findByDocumentId(Long documentId);

    boolean existsByDocumentId(Long documentId);
}
