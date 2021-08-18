package com.task.tracker.api.factory;

import com.task.tracker.api.dto.TaskDto;
import com.task.tracker.api.dto.TaskStateDto;
import com.task.tracker.store.entity.TaskEntity;
import com.task.tracker.store.entity.TaskStateEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskStateDtoFactory {

    public TaskStateDto makeTaskStateDto(TaskStateEntity taskStateEntity){

        return TaskStateDto.builder()
                .id(taskStateEntity.getId())
                .name(taskStateEntity.getName())
                .ordinal(taskStateEntity.getOrdinal())
                .createdAt(taskStateEntity.getCreatedAt())
                .build();

    }
}
