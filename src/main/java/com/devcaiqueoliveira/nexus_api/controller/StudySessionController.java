package com.devcaiqueoliveira.nexus_api.controller;

import com.devcaiqueoliveira.nexus_api.dto.StudySessionResponse;
import com.devcaiqueoliveira.nexus_api.dto.StudySessionStart;
import com.devcaiqueoliveira.nexus_api.service.StudySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<StudySessionResponse> start(@RequestBody @Valid StudySessionStart studySession) {

        StudySessionResponse startedStudySession = studySessionService.startSession(studySession);


        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(startedStudySession.id())
                .toUri();

        return ResponseEntity.created(uri).body(startedStudySession);

    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<StudySessionResponse> finish(@PathVariable UUID id) {

        StudySessionResponse finishedStudySession = studySessionService.finishSession(id);

        return ResponseEntity.ok(finishedStudySession);

    }

    @GetMapping("/{id}")
    public ResponseEntity<StudySessionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(studySessionService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        studySessionService.deleteStudySession(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<StudySessionResponse>> findAllBySubjectId(@RequestParam UUID subjectId, Pageable pageable) {
        return ResponseEntity.ok(studySessionService.findAllBySubjectId(subjectId, pageable));
    }
}
