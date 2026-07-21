package com.hyunsuk.axplatform.museum.repository;

import com.hyunsuk.axplatform.museum.entity.MuseumManual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MuseumManualRepository
        extends JpaRepository<MuseumManual, Long> {

    Optional<MuseumManual> findByDocumentId(Long documentId);
}
