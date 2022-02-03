package com.task.tracker.store.repository;

import com.task.tracker.store.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Stream<ProjectEntity> streamAllByNameContainingIgnoreCase(String name);

    Optional<ProjectEntity> findByName(String name);
}
