package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.dto.UserRequest;
import com.devcaiqueoliveira.nexus_api.dto.UserResponse;
import com.devcaiqueoliveira.nexus_api.entity.User;
import com.devcaiqueoliveira.nexus_api.exception.exceptions.DuplicateResourceException;
import com.devcaiqueoliveira.nexus_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("O e-mail informado já está cadastrado.");
        }

        String encryptedPassword = passwordEncoder.encode(request.password());

        User user = new User(
                null,
                request.name(),
                request.email(),
                encryptedPassword,
                null
        );

        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }
}
