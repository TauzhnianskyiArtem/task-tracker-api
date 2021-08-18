package com.task.tracker.store.repository;

import com.task.tracker.store.entity.ProjectEntity;
import com.task.tracker.store.entity.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {

    Stream<TaskStateEntity> streamAllByNameContainingAndProject(String name, ProjectEntity project);
    Stream<TaskStateEntity> streamAllByProject(ProjectEntity project);

    Optional<TaskStateEntity> findByNameAndProject(String name, ProjectEntity project);
    Optional<TaskStateEntity> findByOrdinalAndProject(Integer ordinal, ProjectEntity project);
}
