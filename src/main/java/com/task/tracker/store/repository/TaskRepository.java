package com.task.tracker.store.repository;

import com.task.tracker.store.entity.TaskEntity;
import com.task.tracker.store.entity.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByTaskState(TaskStateEntity taskState);

}
