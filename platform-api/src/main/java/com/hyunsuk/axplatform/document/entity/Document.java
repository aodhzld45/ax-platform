package com.hyunsuk.axplatform.document.entity;

import com.hyunsuk.axplatform.common.entity.BaseTimeEntity;
import com.hyunsuk.axplatform.common.file.entity.FileMetadata;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "document",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_document_resource_key_version",
                        columnNames = {"resource_key", "version"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_key", nullable = false, length = 100)
    private String resourceKey;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", nullable = false, length = 30)
    private DocumentStatus documentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "index_status", nullable = false, length = 30)
    private DocumentIndexStatus indexStatus;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "file_metadata_id",
            nullable = false,
            unique = true
    )
    private FileMetadata fileMetadata;

    @Builder
    private Document(
            String resourceKey,
            Integer version,
            String title,
            DocumentStatus documentStatus,
            DocumentIndexStatus indexStatus,
            FileMetadata fileMetadata
    ) {
        this.resourceKey = resourceKey;
        this.version = version;
        this.title = title;
        this.documentStatus = documentStatus == null
                ? DocumentStatus.ACTIVE
                : documentStatus;
        this.indexStatus = indexStatus == null
                ? DocumentIndexStatus.NOT_INDEXED
                : indexStatus;
        this.fileMetadata = fileMetadata;
    }

    public void changeStatus(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public void changeIndexStatus(DocumentIndexStatus indexStatus) {
        this.indexStatus = indexStatus;
    }
}
