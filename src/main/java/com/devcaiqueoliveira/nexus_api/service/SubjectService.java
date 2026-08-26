package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.dto.SubjectRequest;
import com.devcaiqueoliveira.nexus_api.dto.SubjectResponse;
import com.devcaiqueoliveira.nexus_api.dto.SubjectUpdateRequest;
import com.devcaiqueoliveira.nexus_api.entity.Subject;
import com.devcaiqueoliveira.nexus_api.entity.User;
import com.devcaiqueoliveira.nexus_api.repository.SubjectRepository;
import com.devcaiqueoliveira.nexus_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    @Transactional
    public SubjectResponse createSubject(SubjectRequest subjectRequest) {

        User user = userRepository.findById(subjectRequest.userId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Subject createdSubject = new Subject(
                subjectRequest.name(),
                subjectRequest.description(),
                subjectRequest.targetHours(),
                user
        );

        Subject savedSubject = subjectRepository.save(createdSubject);

        return new SubjectResponse(savedSubject);
    }

    public List<SubjectResponse> findAllByUserId(UUID userId) {
        return subjectRepository.findAllByUserId(userId).stream()
                .map(SubjectResponse::new)
                .toList();
    }

    public SubjectResponse findById(UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Matéria não encontrada"));
        return new SubjectResponse(subject);
    }

    @Transactional
    public SubjectResponse updateSubject(UUID id, SubjectUpdateRequest subjectRequest) {

        Subject subjectToUpdate = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Matéria não encontrada"));

        subjectToUpdate.setName(subjectRequest.name());
        subjectToUpdate.setDescription(subjectRequest.description());
        subjectToUpdate.setTargetHours(subjectRequest.targetHours());

        Subject savedSubject = subjectRepository.save(subjectToUpdate);

        return new SubjectResponse(savedSubject);
    }

    @Transactional
    public void deleteSubject(UUID id) {

        Subject subjectToDelete = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Matéria não encontrada"));

        subjectRepository.delete(subjectToDelete);
    }

}
