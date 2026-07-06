package com.devcaiqueoliveira.nexus_api.repository;

import com.devcaiqueoliveira.nexus_api.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {
}
