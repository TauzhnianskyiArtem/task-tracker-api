package com.task.tracker.api.controller;


import com.task.tracker.api.controller.helper.ControllerHelper;
import com.task.tracker.api.dto.TaskStateDto;
import com.task.tracker.api.exception.BadRequestException;
import com.task.tracker.store.entity.ProjectEntity;
import com.task.tracker.store.entity.TaskStateEntity;
import com.task.tracker.store.repository.TaskStateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
@RestController
public class TaskStateController {

    ControllerHelper controllerHelper;

    TaskStateRepository taskStateRepository;



    public static final String FETCH_TASK_STATES = "/api/projects/{project_id}/task-states";
    public static final String DELETE_TASK_STATE = "/api/projects/{project_id}/task-states/{task-state_id}";
    public static final String CREATE_OR_UPDATE_TASK_STATE = "/api/projects/{project_id}/task-states";

    @GetMapping(FETCH_TASK_STATES)
    public List<TaskStateDto> fetchTaskStates(
            @PathVariable("project_id") Long projectId){

        ProjectEntity projectEntity = controllerHelper.getProjectEntity(projectId);

        List<TaskStateEntity> taskStates = taskStateRepository.findByProject(projectEntity);

        return taskStates.stream()
                .map(TaskStateDto::makeDefault)
                .collect(Collectors.toList());

    }


    @PostMapping(CREATE_OR_UPDATE_TASK_STATE)
    public TaskStateDto createOrUpdateTaskState(
            @PathVariable("project_id") Long projectId,
            @RequestParam(value = "task-state_id", required = false) Optional<Long> optionalTakStateId,
            @RequestParam(value = "task-state_name", required = false) Optional<String> optionalTaskStateName
    ){

        ProjectEntity projectEntity = controllerHelper.getProjectEntity(projectId);

        optionalTaskStateName = optionalTaskStateName.filter(name -> !name.trim().isEmpty());

        if(!optionalTakStateId.isPresent() && !optionalTaskStateName.isPresent())
            throw new BadRequestException(String.format("Task state name or ordinal can`t be empty"));

        final TaskStateEntity taskState = optionalTakStateId
                .map(controllerHelper::getTaskStateEntity)
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

        TaskStateEntity savedTaskState = taskStateRepository.saveAndFlush(taskState);

        return TaskStateDto.makeDefault(savedTaskState);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    public boolean deleteTaskState(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task-state_id") Long taskStateId){

        controllerHelper.getProjectEntity(projectId);

        controllerHelper.getTaskStateEntity(taskStateId);

        taskStateRepository.deleteById(taskStateId);

        return true;
    }

}
