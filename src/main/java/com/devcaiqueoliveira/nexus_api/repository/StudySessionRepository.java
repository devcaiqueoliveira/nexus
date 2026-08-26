package com.devcaiqueoliveira.nexus_api.repository;

import com.devcaiqueoliveira.nexus_api.entity.StudySession;
import com.devcaiqueoliveira.nexus_api.entity.enums.StudySessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {

    boolean existsBySubjectIdAndStatus(UUID subjectId, StudySessionStatus status);
    Page<StudySession> findAllBySubjectId(UUID subjectId, Pageable pageable);
}
