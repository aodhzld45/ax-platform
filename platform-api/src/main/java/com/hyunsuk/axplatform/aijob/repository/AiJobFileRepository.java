package com.hyunsuk.axplatform.aijob.repository;

import com.hyunsuk.axplatform.aijob.entity.AiJobFile;
import com.hyunsuk.axplatform.aijob.entity.AiJobFileRole;
import com.hyunsuk.axplatform.aijob.entity.AiJobStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiJobFileRepository
        extends JpaRepository<AiJobFile, Long> {

    boolean existsByAiJobIdAndStageAndRole(
            Long aiJobId,
            AiJobStage stage,
            AiJobFileRole role
    );

    List<AiJobFile> findAllByAiJobIdOrderByIdAsc(Long aiJobId);
}
