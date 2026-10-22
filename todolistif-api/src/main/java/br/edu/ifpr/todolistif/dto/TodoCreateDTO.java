package br.edu.ifpr.todolistif.dto;

import java.time.LocalDateTime;

public class TodoCreateDTO {
    private String title;
    private LocalDateTime deadLine;

    public TodoCreateDTO() {
    }

    public TodoCreateDTO(String title, LocalDateTime deadLine) {
        this.title = title;
        this.deadLine = deadLine;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(LocalDateTime deadLine) {
        this.deadLine = deadLine;
    }
}
