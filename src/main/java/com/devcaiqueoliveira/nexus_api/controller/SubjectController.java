package com.devcaiqueoliveira.nexus_api.controller;

import com.devcaiqueoliveira.nexus_api.dto.SubjectRequest;
import com.devcaiqueoliveira.nexus_api.dto.SubjectResponse;
import com.devcaiqueoliveira.nexus_api.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(@RequestBody SubjectRequest subjectRequest) {
        SubjectResponse createdSubject = subjectService.createSubject(subjectRequest);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdSubject.id())
                .toUri();

        return ResponseEntity.created(uri).body(createdSubject);
    }
}
