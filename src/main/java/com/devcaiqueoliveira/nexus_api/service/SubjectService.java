package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.dto.SubjectProgressResponse;
import com.devcaiqueoliveira.nexus_api.dto.SubjectRequest;
import com.devcaiqueoliveira.nexus_api.dto.SubjectResponse;
import com.devcaiqueoliveira.nexus_api.dto.SubjectUpdateRequest;
import com.devcaiqueoliveira.nexus_api.entity.StudySession;
import com.devcaiqueoliveira.nexus_api.entity.Subject;
import com.devcaiqueoliveira.nexus_api.entity.User;
import com.devcaiqueoliveira.nexus_api.entity.enums.StudySessionStatus;
import com.devcaiqueoliveira.nexus_api.repository.StudySessionRepository;
import com.devcaiqueoliveira.nexus_api.repository.SubjectRepository;
import com.devcaiqueoliveira.nexus_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final StudySessionRepository studySessionRepository;

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

    public SubjectProgressResponse getSubjectProgress(UUID subjectId) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Matéria não encontrada"));

        List<StudySession> completedSession = studySessionRepository
                .findAllBySubjectIdAndStatus(subjectId, StudySessionStatus.COMPLETED);

        long totalMinutesStudied = completedSession.stream()
                .mapToLong(session -> Duration.between(session.getStartedAt(), session.getEndedAt()).toMinutes())
                .sum();

        double totalHoursStudied = totalMinutesStudied / 60.0;

        int targetHours = subject.getTargetHours() != null ? subject.getTargetHours() : 0;

        double remainingHours = targetHours - totalHoursStudied;
        if (remainingHours < 0) {
            remainingHours = 0.0;
        }

        double completionPercentage = 0.0;
        if (targetHours > 0) {
            completionPercentage = (totalHoursStudied / targetHours) * 100.0;
            completionPercentage = Math.min(completionPercentage, 100.0);
        }

        return new SubjectProgressResponse(targetHours, totalHoursStudied, remainingHours, completionPercentage);
    }

}
