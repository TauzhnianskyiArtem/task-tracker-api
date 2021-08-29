package com.task.tracker.api.controller.helper;

import com.task.tracker.api.exception.NotFoundException;
import com.task.tracker.store.entity.ProjectEntity;
import com.task.tracker.store.entity.TaskEntity;
import com.task.tracker.store.entity.TaskStateEntity;
import com.task.tracker.store.repository.ProjectRepository;
import com.task.tracker.store.repository.TaskRepository;
import com.task.tracker.store.repository.TaskStateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Component
public class ControllerHelper {

    TaskRepository taskRepository;

    ProjectRepository projectRepository;

    TaskStateRepository taskStateRepository;


    public ProjectEntity getProjectEntityOrThrowException(Long projectId) {

        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with '%s doesn't exist.",
                                        projectId
                                )
                        ));
    }

    public TaskEntity getTaskEntityOrThrowException(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format("Task with '%s' doesn't exist.", taskId)
                        ));
    }

    public TaskStateEntity getTaskStateEntityOrThrowException(Long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format("Task state with '%s' doesn't exist.", taskStateId
                                )));
    }
}
