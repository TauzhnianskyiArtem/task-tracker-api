package com.task.tracker.api.controller;

import com.task.tracker.api.controller.helper.ControllerHelper;
import com.task.tracker.api.dto.TaskDto;
import com.task.tracker.api.exception.BadRequestException;
import com.task.tracker.store.entity.TaskEntity;
import com.task.tracker.store.entity.TaskStateEntity;
import com.task.tracker.store.repository.TaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
@RestController
public class TaskController {

    TaskRepository taskRepository;

    ControllerHelper controllerHelper;


    public static final String FETCH_TASKS = "/api/projects/task-states/{task-state_id}/tasks";
    public static final String DELETE_TASK = "/api/projects/task-states/{task-state_id}/tasks/{task_id}";
    public static final String CREATE_OR_UPDATE_TASK = "/api/projects/task-states/{task-state_id}/tasks";

    @GetMapping(FETCH_TASKS)
    public List<TaskDto> fetchTasks(
            @PathVariable("task-state_id") Long taskStateId) {

        TaskStateEntity taskStateEntity = controllerHelper.getTaskStateEntity(taskStateId);

        List<TaskEntity> tasks = taskRepository.findByTaskState(taskStateEntity);

        return tasks.stream().map(TaskDto::makeDefault).collect(Collectors.toList());

    }

    @PostMapping(CREATE_OR_UPDATE_TASK)
    public TaskDto createOrUpdateTask(
            @PathVariable("task-state_id") Long taskStateId,
            @RequestParam(value = "task_id", required = false) Optional<Long> optionalTaskId,
            @RequestParam(value = "task_name", required = false) Optional<String> optionalTaskName,
            @RequestParam(value = "task_description", required = false) Optional<String> optionalTaskDescription) {

        TaskStateEntity taskStateEntity = controllerHelper.getTaskStateEntity(taskStateId);

        optionalTaskName = optionalTaskName.filter(name -> !name.trim().isEmpty());
        optionalTaskDescription = optionalTaskDescription.filter(description -> !description.trim().isEmpty());

        boolean isCreate = !optionalTaskId.isPresent();

        if (isCreate && !optionalTaskName.isPresent())
            throw new BadRequestException(String.format("Task name can`t be empty"));

        TaskEntity taskEntity = optionalTaskId
                .map(controllerHelper::getTaskEntity)
                .orElseGet(TaskEntity::new);

        taskEntity.setTaskState(taskStateEntity);

        optionalTaskName.ifPresent(taskEntity::setName);

        optionalTaskDescription.ifPresent(taskEntity::setDescription);

        TaskEntity savedTask = taskRepository.saveAndFlush(taskEntity);

        return TaskDto.makeDefault(savedTask);

    }

    @DeleteMapping(DELETE_TASK)
    public boolean deleteTask(
            @PathVariable("task_id") Long taskId,
            @PathVariable("task-state_id") Long taskStateId) {

        controllerHelper.getTaskStateEntity(taskStateId);

        controllerHelper.getTaskEntity(taskId);

        taskRepository.deleteById(taskId);

        return true;
    }

}
