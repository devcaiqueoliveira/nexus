package com.devcaiqueoliveira.nexus_api.repository;

import com.devcaiqueoliveira.nexus_api.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
}
