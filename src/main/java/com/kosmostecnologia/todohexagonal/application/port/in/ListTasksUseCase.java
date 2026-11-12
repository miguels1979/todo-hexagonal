package com.kosmostecnologia.todohexagonal.application.port.in;

import com.kosmostecnologia.todohexagonal.domain.model.Task;

import java.util.List;

public interface ListTasksUseCase {
    List<Task> listAll();
}
