package com.kosmostecnologia.todohexagonal.infrastructure.adapter.in.rest;

import com.kosmostecnologia.todohexagonal.application.port.in.*;
import com.kosmostecnologia.todohexagonal.domain.model.Task;
import com.kosmostecnologia.todohexagonal.infrastructure.adapter.in.rest.dto.CreateTaskRequest;
import com.kosmostecnologia.todohexagonal.infrastructure.adapter.in.rest.dto.TaskResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final GetTaskUseCase getTaskUseCase;
    private final ListTasksUseCase listTasksUseCase;
    private final CompleteTaskUseCase completeTaskUseCase;
    private final ReopenTaskUseCase reopenTaskUseCase;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask( @Valid @RequestBody CreateTaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        Task saved =  createTaskUseCase.create(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listAll(){
        List<TaskResponse> responses =  listTasksUseCase.listAll()
                .stream()
                .map(TaskResponse::from)
                .toList();
        return ResponseEntity.ok(responses);

    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id){
        return ResponseEntity.ok(TaskResponse.from(getTaskUseCase.getById(id)));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable Long id){
        Task task = completeTaskUseCase.complete(id);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PatchMapping("/{id}/reopen")
    public ResponseEntity<TaskResponse> reopenTask(@PathVariable Long id){
        Task task = reopenTaskUseCase.reopen(id);
        return ResponseEntity.ok(TaskResponse.from(task));
    }


}
