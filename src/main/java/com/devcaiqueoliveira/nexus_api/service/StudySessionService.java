package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;

}
