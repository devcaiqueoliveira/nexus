package com.devcaiqueoliveira.nexus_api.controller;

import com.devcaiqueoliveira.nexus_api.AbstractIntegrationTest;
import com.devcaiqueoliveira.nexus_api.dto.AuthenticationRequest;
import com.devcaiqueoliveira.nexus_api.dto.SubjectRequest;
import com.devcaiqueoliveira.nexus_api.dto.UserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StudySessionControllerIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("Deve negar a listagem vazia de sessões de uma matéria pertencente a outro usuário")
    void shouldDenyEmptySessionPageFromAnotherUser() throws Exception {
        String ownerToken = registerAndAuthenticate("owner@nexus.test");
        String otherUserToken = registerAndAuthenticate("other@nexus.test");
        UUID subjectId = createSubject(ownerToken);

        mockMvc.perform(get("/api/study-sessions")
                        .param("subjectId", subjectId.toString())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    private String registerAndAuthenticate(String email) throws Exception {
        UserRequest userRequest = new UserRequest("Integration User", email, "12345678");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(email, "12345678");

        MvcResult authenticationResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(authenticationResult.getResponse().getContentAsString())
                .get("token")
                .asText();
    }

    private UUID createSubject(String token) throws Exception {
        SubjectRequest subjectRequest = new SubjectRequest("Security", "Authorization tests", 20);

        MvcResult subjectResult = mockMvc.perform(post("/api/subjects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subjectRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(objectMapper.readTree(subjectResult.getResponse().getContentAsString())
                .get("id")
                .asText());
    }
}
