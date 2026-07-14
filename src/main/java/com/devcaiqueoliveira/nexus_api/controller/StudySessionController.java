package com.devcaiqueoliveira.nexus_api.controller;

import com.devcaiqueoliveira.nexus_api.dto.StudySessionResponse;
import com.devcaiqueoliveira.nexus_api.dto.StudySessionStart;
import com.devcaiqueoliveira.nexus_api.service.StudySessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/study-sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping
    public ResponseEntity<StudySessionResponse> start(@RequestBody StudySessionStart studySession) {

        StudySessionResponse startedStudySession = studySessionService.startSession(studySession);


        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(startedStudySession.id())
                .toUri();

        return ResponseEntity.created(uri).body(startedStudySession);

    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<StudySessionResponse> finish(UUID id) {

        StudySessionResponse finishedStudySession = studySessionService.finishSession(id);

        return ResponseEntity.ok(finishedStudySession);

    }
}
