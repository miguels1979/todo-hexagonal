package com.kosmostecnologia.todohexagonal.infrastructure.adapter.in.rest;

import com.kosmostecnologia.todohexagonal.application.port.in.CreateTaskUseCase;
import com.kosmostecnologia.todohexagonal.application.service.TaskService;
import com.kosmostecnologia.todohexagonal.domain.model.Task;
import com.kosmostecnologia.todohexagonal.infrastructure.adapter.in.rest.dto.CreateTaskRequest;
import com.kosmostecnologia.todohexagonal.infrastructure.adapter.in.rest.dto.TaskResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask( @Valid @RequestBody CreateTaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        Task saved =  createTaskUseCase.execute(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(saved));
    }


}
