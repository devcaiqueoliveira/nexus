package com.devcaiqueoliveira.nexus_api.controller;

import com.devcaiqueoliveira.nexus_api.dto.UserRequest;
import com.devcaiqueoliveira.nexus_api.dto.UserResponse;
import com.devcaiqueoliveira.nexus_api.entity.User;
import com.devcaiqueoliveira.nexus_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Operações de cadastro e consulta de usuários")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Criar usuário")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest request) {
        UserResponse createdUser = userService.createUser(request);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.id())
                .toUri();

        return ResponseEntity.created(uri).body(createdUser);
    }

    @GetMapping("/me")
    @Operation(summary = "Buscar dados do usuário autenticado")
    public ResponseEntity<UserResponse> getAuthenticatedUser(@AuthenticationPrincipal User loggedUser) {
        return ResponseEntity.ok(userService.getAuthenticatedUser(loggedUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UserResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User loggedUser) {
        return ResponseEntity.ok(userService.findById(id, loggedUser.getId()));
    }
}
