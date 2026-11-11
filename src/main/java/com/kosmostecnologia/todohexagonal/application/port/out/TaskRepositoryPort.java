package com.kosmostecnologia.todohexagonal.application.port.out;

import com.kosmostecnologia.todohexagonal.domain.model.Task;

public interface TaskRepositoryPort {
    Task save(Task task);
}
