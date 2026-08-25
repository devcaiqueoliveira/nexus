package com.devcaiqueoliveira.nexus_api.controller;

import com.devcaiqueoliveira.nexus_api.dto.SubjectRequest;
import com.devcaiqueoliveira.nexus_api.dto.SubjectResponse;
import com.devcaiqueoliveira.nexus_api.service.SubjectService;
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
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(@RequestBody @Valid SubjectRequest subjectRequest) {
        SubjectResponse createdSubject = subjectService.createSubject(subjectRequest);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdSubject.id())
                .toUri();

        return ResponseEntity.created(uri).body(createdSubject);
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> findAllByUserId(@RequestParam UUID userId) {
        List<SubjectResponse> subjects = subjectService.findAllByUserId(userId);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(subjectService.findById(id));
    }
}
