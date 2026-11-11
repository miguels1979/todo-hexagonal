package com.kosmostecnologia.todohexagonal.domain.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {

        super("No se encontró la tarea con ID: " + id);
    }
}
