package com.devcaiqueoliveira.nexus_api.repository;

import com.devcaiqueoliveira.nexus_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
