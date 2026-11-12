package com.kosmostecnologia.todohexagonal.domain.model;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

    @EqualsAndHashCode.Include
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public void complete(){
        if(status == TaskStatus.COMPLETED){
            throw new IllegalStateException("La tarea ya está completada");
        }
        status = TaskStatus.COMPLETED;
        completedAt = LocalDateTime.now();
    }

    public void reopen(){
        if(status == TaskStatus.PENDING){
            throw new IllegalStateException("La tarea ya está pendiente");
        }
        status = TaskStatus.PENDING;
        completedAt = null;
    }

    public void initDefaults(){
        if(status == null) status = TaskStatus.PENDING;
        if(createdAt == null) createdAt = LocalDateTime.now();
    }

}
