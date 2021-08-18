package com.task.tracker.api.factory;

import com.task.tracker.api.dto.ProjectDto;
import com.task.tracker.api.dto.TaskDto;
import com.task.tracker.store.entity.ProjectEntity;
import com.task.tracker.store.entity.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskDtoFactory {

    public TaskDto makeProjectDto(TaskEntity taskEntity){

        return TaskDto.builder()
                .id(taskEntity.getId())
                .name(taskEntity.getName())
                .description(taskEntity.getDescription())
                .createdAt(taskEntity.getCreatedAt())
                .build();

    }
}
