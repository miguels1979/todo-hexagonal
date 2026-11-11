package com.kosmostecnologia.todohexagonal.application.port.out;

import com.kosmostecnologia.todohexagonal.domain.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepositoryPort {
    Task save(Task task);
    List<Task> findAll();
    Optional<Task> findById(Long id);
    Task update(Task task);
}
