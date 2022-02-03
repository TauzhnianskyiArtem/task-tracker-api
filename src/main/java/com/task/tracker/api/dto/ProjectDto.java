package com.task.tracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.task.tracker.store.entity.ProjectEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectDto {

    @NonNull
    Long id;

    @NonNull
    String name;

    @NonNull
    @JsonProperty("created_at")
    Instant createdAt;

    public static ProjectDto makeDefault(ProjectEntity entity){

        return ProjectDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
