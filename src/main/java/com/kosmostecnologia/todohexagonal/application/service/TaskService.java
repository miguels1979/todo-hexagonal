package com.kosmostecnologia.todohexagonal.application.service;

import com.kosmostecnologia.todohexagonal.application.port.in.CreateTaskUseCase;
import com.kosmostecnologia.todohexagonal.application.port.out.TaskRepositoryPort;
import com.kosmostecnologia.todohexagonal.domain.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TaskService implements CreateTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    @Override
    public Task execute(Task task) {
        return taskRepositoryPort.save(task) ;
    }
}
