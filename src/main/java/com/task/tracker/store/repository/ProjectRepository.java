package com.task.tracker.store.repository;

import com.task.tracker.store.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Stream<ProjectEntity> streamAllByNameContainingIgnoreCase(String name);

    Stream<ProjectEntity> streamAllBy();

    Optional<ProjectEntity> findByName(String name);
}
