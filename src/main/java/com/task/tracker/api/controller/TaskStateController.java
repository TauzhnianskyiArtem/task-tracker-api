package com.task.tracker.api.controller;


import com.task.tracker.api.dto.AckDto;
import com.task.tracker.api.dto.TaskStateDto;
import com.task.tracker.api.exception.BadRequestException;
import com.task.tracker.api.exception.NotFoundException;
import com.task.tracker.api.factory.TaskStateDtoFactory;
import com.task.tracker.store.entity.ProjectEntity;
import com.task.tracker.store.entity.TaskStateEntity;
import com.task.tracker.store.repository.ProjectRepository;
import com.task.tracker.store.repository.TaskStateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
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
public class TaskStateController {

    TaskStateRepository taskStateRepository;

    TaskStateDtoFactory taskStateDtoFactory;

    ProjectRepository projectRepository;

    public static final String FETCH_TASK_STATES = "/api/projects/{project_id}/task_states";
    public static final String DELETE_TASK_STATE = "/api/projects/{project_id}/task_states/{task_state_id}";
    public static final String CREATE_OR_UPDATE_TASK_STATE = "/api/projects/{project_id}/task_states";

    @GetMapping(FETCH_TASK_STATES)
    public ResponseEntity<List<TaskStateDto>> fetchTaskStates(
            @PathVariable("project_id") Long projectId,
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName){

        ProjectEntity projectEntity = getProjectEntityOrThrowException(projectId);

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<TaskStateEntity> taskStateStream = optionalPrefixName
                .map(prefixName -> taskStateRepository
                        .streamAllByNameContainingAndProject(prefixName, projectEntity))
                .orElseGet(() -> taskStateRepository
                        .streamAllByProject(projectEntity));

        return ResponseEntity.ok(taskStateStream
                .map(taskStateDtoFactory::makeTaskStateDto)
                .collect(Collectors.toList()));

    }


    @PostMapping(CREATE_OR_UPDATE_TASK_STATE)
    public TaskStateDto createOrUpdateTaskState(
            @PathVariable("project_id") Long projectId,
            @RequestParam(value = "task_state_id", required = false) Optional<Long> optionalTakStateId,
            @RequestParam(value = "task_state_name", required = false) Optional<String> optionalTaskStateName,
            @RequestParam(value = "task_state_ordinal", required = false) Optional<Integer> optionalTaskStateOrdinal){

        ProjectEntity projectEntity = getProjectEntityOrThrowException(projectId);

        optionalTaskStateName = optionalTaskStateName.filter(name -> !name.trim().isEmpty());

        boolean isCreate = !optionalTakStateId.isPresent();

        if(isCreate && !optionalTaskStateName.isPresent() && !optionalTaskStateOrdinal.isPresent())
            throw new BadRequestException(String.format("Task state name or ordinal can`t be empty"));

        final TaskStateEntity taskState = optionalTakStateId
                .map(this::getTaskStateEntityOrThrowException)
                .orElseGet(TaskStateEntity::new);

        taskState.setProject(projectEntity);

        optionalTaskStateName
                .ifPresent(taskStateName -> {

                    taskStateRepository
                            .findByNameAndProject(taskStateName, projectEntity)
                            .filter(anotherTaskState -> !Objects.equals(anotherTaskState.getId(), taskState.getId()))
                            .ifPresent((anotherTaskState) -> {
                                 throw new BadRequestException(
                                        String.format("Task state '%s' already exists.", taskStateName)
                                );
                            });
                    taskState.setName(taskStateName);
                } );

        optionalTaskStateOrdinal
                .ifPresent(taskStateOrdinal -> {

                    taskStateRepository
                            .findByOrdinalAndProject(taskStateOrdinal, projectEntity)
                            .filter(anotherTaskState -> !Objects.equals(anotherTaskState.getId(), taskState.getId()))
                            .ifPresent((anotherTaskState) -> {
                                 throw  new BadRequestException(
                                        String.format("Task state with '%s' ordinal already exists.", taskStateOrdinal)
                                );
                            });
                    taskState.setOrdinal(taskStateOrdinal);
                } );

        TaskStateEntity savedTaskState = taskStateRepository.saveAndFlush(taskState);

        return taskStateDtoFactory.makeTaskStateDto(savedTaskState);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    public ResponseEntity<AckDto> deleteTaskState(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task_state_id") Long taskStateId){

        getProjectEntityOrThrowException(projectId);

        getTaskStateEntityOrThrowException(taskStateId);

        taskStateRepository.deleteById(taskStateId);

        return ResponseEntity.ok(AckDto.makeDefault(true));
    }

    private ProjectEntity getProjectEntityOrThrowException(Long projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format("Project with '%s' doesn't exist.", projectId
                                )));
    }

    private TaskStateEntity getTaskStateEntityOrThrowException(Long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format("Task state with '%s' doesn't exist.", taskStateId
                                )));
    }

}
