package com.hyunsuk.axplatform.medical.repository;

import com.hyunsuk.axplatform.medical.entity.MedicalManual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalManualRepository
        extends JpaRepository<MedicalManual, Long> {

    Optional<MedicalManual> findByDocumentId(Long documentId);
}
