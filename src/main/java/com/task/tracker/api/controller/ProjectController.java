package com.task.tracker.api.controller;

import com.task.tracker.api.controller.helper.ControllerHelper;
import com.task.tracker.api.dto.ProjectDto;
import com.task.tracker.api.exception.BadRequestException;
import com.task.tracker.store.entity.ProjectEntity;
import com.task.tracker.store.repository.ProjectRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
@RestController
public class ProjectController {

    ControllerHelper controllerHelper;


    ProjectRepository projectRepository;


    public static final String FETCH_PROJECTS = "/api/projects";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";

    @GetMapping(FETCH_PROJECTS)
    public List<ProjectDto> fetchProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName
    ) {
        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());
        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameContainingIgnoreCase)
                .orElseGet(() -> projectRepository.findAll().stream());

        return projectStream
                .map(ProjectDto::makeDefault)
                .collect(Collectors.toList());

    }

    @PostMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName
    ){

        optionalProjectName = optionalProjectName.filter(name -> !name.trim().isEmpty());

        boolean isCreate = !optionalProjectId.isPresent();

        if (isCreate && !optionalProjectName.isPresent())
            throw new BadRequestException(String.format("Project name can`t be empty"));

        final ProjectEntity project = optionalProjectId
                .map(controllerHelper::getProjectEntity)
                .orElseGet(ProjectEntity::new);

        optionalProjectName
                .ifPresent(projectName -> {

                    projectRepository
                            .findByName(projectName)
                            .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()))
                            .ifPresent((anotherProject) ->{
                                 throw new BadRequestException(
                                        String.format("Project '%s' already exists.", projectName)
                                );
                            });

                    project.setName(projectName);
                });

        ProjectEntity savedProject = projectRepository.saveAndFlush(project);

        return ProjectDto.makeDefault(savedProject);

    }


    @DeleteMapping(DELETE_PROJECT)
    public boolean deleteProject(@PathVariable("project_id") Long projectId) {

        controllerHelper.getProjectEntity(projectId);

        projectRepository.deleteById(projectId);

        return true;
    }


}
