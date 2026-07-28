package com.hyunsuk.axplatform.aijob.entity;

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
import jakarta.persistence.ManyToOne;
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
        name = "ai_job_file",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ai_job_file_job_stage_role",
                        columnNames = {"ai_job_id", "stage", "role"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiJobFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_job_id", nullable = false)
    private AiJob aiJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 50)
    private AiJobStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private AiJobFileRole role;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "file_metadata_id",
            nullable = false,
            unique = true
    )
    private FileMetadata fileMetadata;

    @Builder
    private AiJobFile(
            AiJob aiJob,
            AiJobStage stage,
            AiJobFileRole role,
            FileMetadata fileMetadata
    ) {
        this.aiJob = aiJob;
        this.stage = stage;
        this.role = role;
        this.fileMetadata = fileMetadata;
    }

    public static AiJobFile create(
            AiJob aiJob,
            AiJobStage stage,
            AiJobFileRole role,
            FileMetadata fileMetadata
    ) {
        return AiJobFile.builder()
                .aiJob(aiJob)
                .stage(stage)
                .role(role)
                .fileMetadata(fileMetadata)
                .build();
    }
}
