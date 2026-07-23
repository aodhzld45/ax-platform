package com.hyunsuk.axplatform.document.repository;

import com.hyunsuk.axplatform.document.entity.Document;
import com.hyunsuk.axplatform.document.entity.DocumentIndexStatus;
import com.hyunsuk.axplatform.document.entity.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    @EntityGraph(attributePaths = "fileMetadata")
    @Query("""
            select document
            from Document document
            where (:documentStatus is null
                    or document.documentStatus = :documentStatus)
              and (:indexStatus is null
                    or document.indexStatus = :indexStatus)
            order by document.id desc
            """)
    Page<Document> findAllByFilters(
            @Param("documentStatus") DocumentStatus documentStatus,
            @Param("indexStatus") DocumentIndexStatus indexStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "fileMetadata")
    Optional<Document> findWithFileMetadataById(Long id);

    Optional<Document> findByResourceKeyAndVersion(
            String resourceKey,
            Integer version
    );
}
