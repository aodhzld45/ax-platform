package com.hyunsuk.axplatform.korean.entity;

import com.hyunsuk.axplatform.common.entity.BaseTimeEntity;
import com.hyunsuk.axplatform.document.entity.Document;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "korean_source_document")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KoreanSourceDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(name = "source_domain", length = 100)
    private String sourceDomain;

    @Column(name = "description", length = 500)
    private String description;

    @Builder
    private KoreanSourceDocument(
            Document document,
            String sourceDomain,
            String description
    ) {
        this.document = document;
        this.sourceDomain = sourceDomain;
        this.description = description;
    }
}
