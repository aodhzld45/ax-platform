package com.hyunsuk.axplatform.document.repository;

import com.hyunsuk.axplatform.document.entity.Document;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    @EntityGraph(attributePaths = "fileMetadata")
    List<Document> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "fileMetadata")
    Optional<Document> findWithFileMetadataById(Long id);

    Optional<Document> findByResourceKeyAndVersion(
            String resourceKey,
            Integer version
    );
}
