package br.edu.ifpr.todolistif.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.ifpr.todolistif.dto.TodoConclusaoDTO;
import br.edu.ifpr.todolistif.dto.TodoCreateDTO;
import br.edu.ifpr.todolistif.dto.TodoResponseDTO;
import br.edu.ifpr.todolistif.model.Todo;
import br.edu.ifpr.todolistif.repository.TodoRepository;

@Service 
public class TodoService {
    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public TodoResponseDTO criar(TodoCreateDTO dto) {
        Todo todo = new Todo();
        todo.setTitle(dto.getTitle());
        todo.setDeadLine(dto.getDeadLine());
        Todo salvo = repository.save(todo);
        return new TodoResponseDTO(salvo);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public TodoResponseDTO marcarConclusao(Long id, TodoConclusaoDTO dto) {
        Todo todo = repository.findById(id).orElseThrow();
        todo.setFinishedAt(dto.getFinishedAt());
        Todo atualizado = repository.save(todo);
        return new TodoResponseDTO(atualizado);
    }

    public List<TodoResponseDTO> listarConcluidas() {
        List<Todo> tarefas = repository.findByFinishedAtIsNotNull();
        List<TodoResponseDTO> resposta = new ArrayList<>();
        for (Todo tarefa : tarefas) {
            resposta.add(new TodoResponseDTO(tarefa));
        }
        return resposta;
    }

    public List<TodoResponseDTO> listarPendentes() {
        List<Todo> tarefas = repository.findByFinishedAtIsNull();
        List<TodoResponseDTO> resposta = new ArrayList<>();
        for (Todo tarefa : tarefas) {
            resposta.add(new TodoResponseDTO(tarefa));
        }
        return resposta;
    }

    public List<TodoResponseDTO> listar() {
        List<Todo> tarefas = repository.findAll();
        List<TodoResponseDTO> resposta = new ArrayList<>();
        for (Todo tarefa : tarefas) {
            resposta.add(new TodoResponseDTO(tarefa));
        }
        return resposta;
    }
}
