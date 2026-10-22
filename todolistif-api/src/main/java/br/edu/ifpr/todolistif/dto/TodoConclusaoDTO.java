package br.edu.ifpr.todolistif.dto;

import java.time.LocalDateTime;

public class TodoConclusaoDTO {
    private LocalDateTime finishedAt;

    public TodoConclusaoDTO() {
    }

    public TodoConclusaoDTO(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
