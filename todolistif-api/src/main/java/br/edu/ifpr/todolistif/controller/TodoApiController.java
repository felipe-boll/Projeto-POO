package br.edu.ifpr.todolistif.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifpr.todolistif.dto.TodoConclusaoDTO;
import br.edu.ifpr.todolistif.dto.TodoCreateDTO;
import br.edu.ifpr.todolistif.dto.TodoResponseDTO;
import br.edu.ifpr.todolistif.service.TodoService;

@RestController
@RequestMapping("/api/todos")
public class TodoApiController {

    private final TodoService service;

    public TodoApiController(TodoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TodoResponseDTO> criar(@RequestBody TodoCreateDTO dto) {
        TodoResponseDTO criado = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/conclusao")
    public ResponseEntity<TodoResponseDTO> informarConclusao(
            @PathVariable Long id,
            @RequestBody TodoConclusaoDTO dto) {
        TodoResponseDTO atualizado = service.marcarConclusao(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @GetMapping("/concluidas")
    public ResponseEntity<List<TodoResponseDTO>> listarConcluidas() {
        List<TodoResponseDTO> concluidas = service.listarConcluidas();
        return ResponseEntity.ok(concluidas);
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<TodoResponseDTO>> listarPendentes() {
        List<TodoResponseDTO> pendentes = service.listarPendentes();
        return ResponseEntity.ok(pendentes);
    }

    @GetMapping
    public ResponseEntity<List<TodoResponseDTO>> listarTodas() {
        List<TodoResponseDTO> todas = service.listar();
        return ResponseEntity.ok(todas);
    }
}
