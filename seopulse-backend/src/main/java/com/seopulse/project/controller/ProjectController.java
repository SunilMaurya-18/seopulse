package com.seopulse.project.controller;

import com.seopulse.common.dto.PageResponse;
import com.seopulse.project.dto.CreateProjectRequest;
import com.seopulse.project.dto.ProjectResponse;
import com.seopulse.project.service.ProjectService;
import com.seopulse.user.entity.User;
import com.seopulse.user.repository.UserRepository;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        ProjectResponse response =
                projectService.createProject(
                        request,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public PageResponse<ProjectResponse> getProjects(
            Pageable pageable,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return projectService.getProjects(
                userId,
                pageable
        );
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return projectService.getProject(
                projectId,
                userId
        );
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        projectService.deleteProject(
                projectId,
                userId
        );
    }

    private Long getUserId(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );

        return user.getId();
    }
}