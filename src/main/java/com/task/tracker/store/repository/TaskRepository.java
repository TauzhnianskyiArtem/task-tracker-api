package com.task.tracker.store.repository;

import com.task.tracker.store.entity.TaskEntity;
import com.task.tracker.store.entity.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    Stream<TaskEntity> streamByNameContainingAndTaskState(String name, TaskStateEntity taskState);

    Stream<TaskEntity> streamAllByTaskState(TaskStateEntity taskState);

    Optional<TaskEntity> findByName(String name);
}
