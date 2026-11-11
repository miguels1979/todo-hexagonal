package com.kosmostecnologia.todohexagonal.infrastructure.adapter.in.rest.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateTaskRequest {

    @NotBlank(message = "El titulo es obligatorio")
    private String title;

    private String description;


}
