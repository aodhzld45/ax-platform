package com.hyunsuk.axplatform.document.repository;

import com.hyunsuk.axplatform.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    Optional<Document> findByResourceKeyAndVersion(
            String resourceKey,
            Integer version
    );
}
