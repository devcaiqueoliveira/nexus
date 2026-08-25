package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.dto.SubjectRequest;
import com.devcaiqueoliveira.nexus_api.dto.SubjectResponse;
import com.devcaiqueoliveira.nexus_api.entity.Subject;
import com.devcaiqueoliveira.nexus_api.entity.User;
import com.devcaiqueoliveira.nexus_api.repository.SubjectRepository;
import com.devcaiqueoliveira.nexus_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    @Transactional
    public SubjectResponse createSubject(SubjectRequest subjectRequest) {

        User user = userRepository.findById(subjectRequest.userId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + subjectRequest.userId()));

        Subject createdSubject = new Subject(
                subjectRequest.name(),
                subjectRequest.description(),
                subjectRequest.targetHours(),
                user
        );

        Subject savedSubject = subjectRepository.save(createdSubject);

        return new SubjectResponse(savedSubject);
    }

}
