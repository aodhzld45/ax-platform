package com.hyunsuk.axplatform.medical.entity;

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
@Table(name = "medical_manual")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicalManual extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "manual_category", length = 100)
    private String manualCategory;

    @Builder
    private MedicalManual(
            Document document,
            String department,
            String manualCategory
    ) {
        this.document = document;
        this.department = department;
        this.manualCategory = manualCategory;
    }
}
