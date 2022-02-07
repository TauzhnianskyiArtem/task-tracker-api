package com.task.tracker.store.repository;

import com.task.tracker.store.entity.ProjectEntity;
import com.task.tracker.store.entity.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {

    List<TaskStateEntity> findByProject(ProjectEntity project);

    Optional<TaskStateEntity> findByNameAndProject(String name, ProjectEntity project);
}
