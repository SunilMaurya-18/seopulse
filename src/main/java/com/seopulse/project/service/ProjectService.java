package com.seopulse.project.service;

import com.seopulse.common.dto.PageResponse;
import com.seopulse.common.exception.ResourceNotFoundException;
import com.seopulse.project.dto.CreateProjectRequest;
import com.seopulse.project.dto.ProjectResponse;
import com.seopulse.project.entity.Project;
import com.seopulse.project.repository.ProjectRepository;
import com.seopulse.user.entity.User;
import com.seopulse.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectResponse createProject(
            CreateProjectRequest request,
            Long userId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Project project = Project.builder()
                .name(request.name().trim())
                .description(
                        request.description() == null
                                ? null
                                : request.description().trim()
                )
                .user(user)
                .build();

        Project savedProject =
                projectRepository.save(project);

        return mapToResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getProjects(
            Long userId,
            Pageable pageable
    ) {

        Page<ProjectResponse> page =
                projectRepository
                        .findByUserId(userId, pageable)
                        .map(this::mapToResponse);

        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(
            Long projectId,
            Long userId
    ) {

        Project project =
                projectRepository
                        .findById(projectId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                )
                        );

        verifyOwnership(project, userId);

        return mapToResponse(project);
    }

    public void deleteProject(
            Long projectId,
            Long userId
    ) {

        Project project =
                projectRepository
                        .findById(projectId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                )
                        );

        verifyOwnership(project, userId);

        projectRepository.delete(project);
    }

    private void verifyOwnership(
            Project project,
            Long userId
    ) {

        if (!project.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Project not found"
            );
        }
    }

    private ProjectResponse mapToResponse(
            Project project
    ) {

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}