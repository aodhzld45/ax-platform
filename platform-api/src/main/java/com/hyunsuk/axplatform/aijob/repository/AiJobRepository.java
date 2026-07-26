package com.hyunsuk.axplatform.aijob.repository;

import com.hyunsuk.axplatform.aijob.entity.AiJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiJobRepository extends JpaRepository<AiJob, Long> {

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Optional<AiJob> findByJobKey(String jobKey);

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Page<AiJob> findAllByDocumentIdOrderByIdDesc(
            Long documentId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "document",
            "document.fileMetadata"
    })
    Optional<AiJob> findFirstByDocumentIdOrderByIdDesc(Long documentId);
}
