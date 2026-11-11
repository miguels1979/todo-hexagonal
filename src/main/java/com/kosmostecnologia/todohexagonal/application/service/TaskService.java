package com.kosmostecnologia.todohexagonal.application.service;

import com.kosmostecnologia.todohexagonal.application.port.in.CreateTaskUseCase;
import com.kosmostecnologia.todohexagonal.application.port.in.GetTaskUseCase;
import com.kosmostecnologia.todohexagonal.application.port.in.ListTasksUseCase;
import com.kosmostecnologia.todohexagonal.application.port.out.TaskRepositoryPort;
import com.kosmostecnologia.todohexagonal.domain.exception.TaskNotFoundException;
import com.kosmostecnologia.todohexagonal.domain.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TaskService implements CreateTaskUseCase , GetTaskUseCase, ListTasksUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    @Override
    public Task create(Task task) {
        return taskRepositoryPort.save(task) ;
    }

    @Override
    public Task getById(Long id) {
        return taskRepositoryPort.findById(id).orElseThrow(
                ()-> new TaskNotFoundException(id)
        );
    }

    @Override
    public List<Task> listAll() {
        return taskRepositoryPort.findAll();
    }
}
