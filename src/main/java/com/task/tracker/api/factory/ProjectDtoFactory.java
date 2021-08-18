package com.task.tracker.api.factory;

import com.task.tracker.api.dto.ProjectDto;
import com.task.tracker.store.entity.ProjectEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectDtoFactory {

    public ProjectDto makeProjectDto(ProjectEntity projectEntity){

        return ProjectDto.builder()
                .id(projectEntity.getId())
                .name(projectEntity.getName())
                .createdAt(projectEntity.getCreatedAt())
                .build();

    }
}
