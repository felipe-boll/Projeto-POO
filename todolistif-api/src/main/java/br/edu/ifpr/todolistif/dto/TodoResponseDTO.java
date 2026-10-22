package br.edu.ifpr.todolistif.dto;

import java.time.LocalDateTime;

import br.edu.ifpr.todolistif.model.Todo;

public class TodoResponseDTO {
    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;
    private final LocalDateTime deadLine;
    private final LocalDateTime finishedAt;

    public TodoResponseDTO(Todo todo) {
        this.id = todo.getId();
        this.title = todo.getTitle();
        this.createdAt = todo.getCreatedAt();
        this.deadLine = todo.getDeadLine();
        this.finishedAt = todo.getFinishedAt();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeadLine() {
        return deadLine;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }
}
