package com.devcaiqueoliveira.nexus_api.controller;

import com.devcaiqueoliveira.nexus_api.dto.StudySessionResponse;
import com.devcaiqueoliveira.nexus_api.dto.StudySessionStart;
import com.devcaiqueoliveira.nexus_api.entity.User;
import com.devcaiqueoliveira.nexus_api.service.StudySessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/study-sessions")
@RequiredArgsConstructor
@Tag(name = "Sessões de Estudos", description = "Operações de inicialização, finalização, deleção, leitura e paginação de sessões de estudo")
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping
    @Operation(summary = "Iniciar sessão de estudo")
    public ResponseEntity<StudySessionResponse> start(
            @RequestBody @Valid StudySessionStart studySession,
            @AuthenticationPrincipal User loggedUser) {

        StudySessionResponse startedStudySession = studySessionService.startSession(studySession, loggedUser.getId());


        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(startedStudySession.id())
                .toUri();

        return ResponseEntity.created(uri).body(startedStudySession);

    }

    @PatchMapping("/{id}/finish")
    @Operation(summary = "Finalizar sessão de estudo")
    public ResponseEntity<StudySessionResponse> finish(
            @PathVariable UUID id,
            @AuthenticationPrincipal User loggedUser) {

        StudySessionResponse finishedStudySession = studySessionService.finishSession(id, loggedUser.getId());

        return ResponseEntity.ok(finishedStudySession);

    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sessão de estudo por ID")
    public ResponseEntity<StudySessionResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User loggedUser) {

        return ResponseEntity.ok(studySessionService.findById(id, loggedUser.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar sessão de estudo por ID")
    public ResponseEntity<Void> deleteById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User loggedUser) {
        studySessionService.deleteStudySession(id, loggedUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar sessões de estudo por disciplina")
    public ResponseEntity<Page<StudySessionResponse>> findAllBySubjectId(
            @RequestParam UUID subjectId,
            Pageable pageable,
            @AuthenticationPrincipal User loggedUser) {

        return ResponseEntity.ok(studySessionService.findAllBySubjectId(subjectId, pageable, loggedUser.getId()));
    }
}
