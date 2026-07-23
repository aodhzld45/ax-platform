package com.hyunsuk.axplatform.medical.repository;

import com.hyunsuk.axplatform.medical.entity.MedicalManual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalManualRepository
        extends JpaRepository<MedicalManual, Long> {

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Page<MedicalManual> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Optional<MedicalManual> findWithDocumentById(Long id);

    Optional<MedicalManual> findByDocumentId(Long documentId);

    boolean existsByDocumentId(Long documentId);
}
