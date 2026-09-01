package com.devcaiqueoliveira.nexus_api.controller;

import com.devcaiqueoliveira.nexus_api.dto.SubjectProgressResponse;
import com.devcaiqueoliveira.nexus_api.dto.SubjectRequest;
import com.devcaiqueoliveira.nexus_api.dto.SubjectResponse;
import com.devcaiqueoliveira.nexus_api.dto.SubjectUpdateRequest;
import com.devcaiqueoliveira.nexus_api.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Matérias", description = "Operações de criação, deleção, atualização e consulta de matérias")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @Operation(summary = "Criar nova matéria")
    public ResponseEntity<SubjectResponse> createSubject(@RequestBody @Valid SubjectRequest subjectRequest) {
        SubjectResponse createdSubject = subjectService.createSubject(subjectRequest);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdSubject.id())
                .toUri();

        return ResponseEntity.created(uri).body(createdSubject);
    }

    @GetMapping
    @Operation(summary = "Listar matérias de usuário")
    public ResponseEntity<List<SubjectResponse>> findAllByUserId(@RequestParam UUID userId) {
        List<SubjectResponse> subjects = subjectService.findAllByUserId(userId);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar matéria por ID")
    public ResponseEntity<SubjectResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(subjectService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar matéria")
    public ResponseEntity<SubjectResponse> updateSubject(@PathVariable UUID id, @RequestBody @Valid SubjectUpdateRequest subjectRequest) {
        return ResponseEntity.ok(subjectService.updateSubject(id, subjectRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar matéria")
    public ResponseEntity<Void> deleteSubject(@PathVariable UUID id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Obter progresso de horas estudadas da matéria")
    public ResponseEntity<SubjectProgressResponse> getSubjectProgress(@PathVariable UUID id) {
        SubjectProgressResponse subjectProgress = subjectService.getSubjectProgress(id);
        return ResponseEntity.ok(subjectProgress);
    }
}
