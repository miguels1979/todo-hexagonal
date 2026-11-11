package com.kosmostecnologia.todohexagonal.application.port.in;

import com.kosmostecnologia.todohexagonal.domain.model.Task;

public interface CreateTaskUseCase {

    Task execute(Task task);

}
