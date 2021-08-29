package com.task.tracker.api.controller;

import com.task.tracker.api.controller.helper.ControllerHelper;
import com.task.tracker.api.dto.AckDto;
import com.task.tracker.api.dto.TaskDto;
import com.task.tracker.api.exception.BadRequestException;
import com.task.tracker.api.factory.TaskDtoFactory;
import com.task.tracker.store.entity.TaskEntity;
import com.task.tracker.store.entity.TaskStateEntity;
import com.task.tracker.store.repository.TaskRepository;
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
public class TaskController {

    TaskDtoFactory taskDtoFactory;

    TaskRepository taskRepository;

    ControllerHelper controllerHelper;


    public static final String FETCH_TASKS = "/api/projects/task-states/{task-state_id}/tasks";
    public static final String DELETE_TASK = "/api/projects/task-states/{task-state_id}/tasks/{task_id}";
    public static final String CREATE_OR_UPDATE_TASK = "/api/projects/task-states/{task-state_id}/tasks";

    @GetMapping(FETCH_TASKS)
    public ResponseEntity<List<TaskDto>> fetchTasks(
            @PathVariable("task-state_id") Long taskStateId,
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        TaskStateEntity taskStateEntity = controllerHelper.getTaskStateEntityOrThrowException(taskStateId);

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<TaskEntity> taskStream = optionalPrefixName
                .map((prefixName) -> taskRepository
                        .streamByNameContainingAndTaskState(prefixName, taskStateEntity))
                .orElseGet(() -> taskRepository
                        .streamAllByTaskState(taskStateEntity));

        return ResponseEntity.ok(
                taskStream.map(taskDtoFactory::makeProjectDto).collect(Collectors.toList()));

    }

    @PostMapping(CREATE_OR_UPDATE_TASK)
    public ResponseEntity<TaskDto> createOrUpdateTask(
            @PathVariable("task-state_id") Long taskStateId,
            @RequestParam(value = "task_id", required = false) Optional<Long> optionalTaskId,
            @RequestParam(value = "task_name", required = false) Optional<String> optionalTaskName,
            @RequestParam(value = "task_description", required = false) Optional<String> optionalTaskDescription) {

        TaskStateEntity taskStateEntity = controllerHelper.getTaskStateEntityOrThrowException(taskStateId);

        optionalTaskName = optionalTaskName.filter(name -> !name.trim().isEmpty());
        optionalTaskDescription = optionalTaskDescription.filter(description -> !description.trim().isEmpty());

        boolean isCreate = !optionalTaskId.isPresent();

        if (isCreate && !optionalTaskName.isPresent())
            throw new BadRequestException(String.format("Task name can`t be empty"));

        TaskEntity taskEntity = optionalTaskId
                .map(controllerHelper::getTaskEntityOrThrowException)
                .orElseGet(TaskEntity::new);

        taskEntity.setTaskState(taskStateEntity);
        optionalTaskName.ifPresent((taskName) -> {
            taskRepository
                    .findByName(taskName)
                    .filter((anotherTask) -> !Objects.equals(anotherTask.getId(), taskEntity.getId()))
                    .ifPresent((anotherTask) -> {
                        throw new BadRequestException(
                                String.format("Task state '%s' already exists.", taskName)
                        );
                    });
            taskEntity.setName(taskName);
        });

        optionalTaskDescription.ifPresent(taskEntity::setDescription);

        TaskEntity savedTask = taskRepository.saveAndFlush(taskEntity);

        return ResponseEntity.ok(taskDtoFactory.makeProjectDto(savedTask));

    }

    @DeleteMapping(DELETE_TASK)
    public ResponseEntity<AckDto> deleteTask(
            @PathVariable("task_id") Long taskId,
            @PathVariable("task-state_id") Long taskStateId) {

        controllerHelper.getTaskStateEntityOrThrowException(taskStateId);

        controllerHelper.getTaskEntityOrThrowException(taskId);

        taskRepository.deleteById(taskId);
        return ResponseEntity.ok(AckDto.makeDefault(true));
    }

}
