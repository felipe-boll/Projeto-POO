package br.edu.ifpr.todolistif;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpr.todolistif.dto.TodoConclusaoDTO;
import br.edu.ifpr.todolistif.dto.TodoCreateDTO;
import br.edu.ifpr.todolistif.dto.TodoResponseDTO;
import br.edu.ifpr.todolistif.model.Todo;
import br.edu.ifpr.todolistif.repository.TodoRepository;
import br.edu.ifpr.todolistif.service.TodoService;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository repository;

    @InjectMocks
    private TodoService service;

    private Todo todo;

    @BeforeEach
    void setUp() {
        todo = new Todo();
        todo.setId(1L);
        todo.setTitle("Estudar Spring Boot");
        todo.setDeadLine(LocalDateTime.of(2026, 10, 30, 18, 0, 0));
    }

    @Test
    void deveCriarTarefaComSucesso() {
        TodoCreateDTO createDTO = new TodoCreateDTO("Estudar Spring Boot", LocalDateTime.of(2026, 10, 30, 18, 0, 0));

        when(repository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        TodoResponseDTO response = service.criar(createDTO);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Estudar Spring Boot", response.getTitle());
        assertEquals(LocalDateTime.of(2026, 10, 30, 18, 0, 0), response.getDeadLine());
        assertNotNull(response.getCreatedAt());
        assertNull(response.getFinishedAt());
        verify(repository, times(1)).save(any(Todo.class));
    }

    @Test
    void deveExcluirTarefaComSucesso() {
        service.excluir(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deveMarcarConclusaoPreservandoDemaisDados() {
        LocalDateTime conclusao = LocalDateTime.of(2026, 10, 22, 10, 30, 0);
        TodoConclusaoDTO conclusaoDTO = new TodoConclusaoDTO(conclusao);

        when(repository.findById(1L)).thenReturn(Optional.of(todo));
        when(repository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TodoResponseDTO response = service.marcarConclusao(1L, conclusaoDTO);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Estudar Spring Boot", response.getTitle());
        assertEquals(LocalDateTime.of(2026, 10, 30, 18, 0, 0), response.getDeadLine());
        assertEquals(conclusao, response.getFinishedAt());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(todo);
    }

    @Test
    void deveListarTarefasConcluidas() {
        todo.setFinishedAt(LocalDateTime.of(2026, 10, 22, 10, 30, 0));
        when(repository.findByFinishedAtIsNotNull()).thenReturn(List.of(todo));

        List<TodoResponseDTO> concluidas = service.listarConcluidas();

        assertEquals(1, concluidas.size());
        assertEquals(1L, concluidas.get(0).getId());
        assertNotNull(concluidas.get(0).getFinishedAt());
        verify(repository, times(1)).findByFinishedAtIsNotNull();
    }

    @Test
    void deveListarTarefasPendentes() {
        when(repository.findByFinishedAtIsNull()).thenReturn(List.of(todo));

        List<TodoResponseDTO> pendentes = service.listarPendentes();

        assertEquals(1, pendentes.size());
        assertEquals(1L, pendentes.get(0).getId());
        assertNull(pendentes.get(0).getFinishedAt());
        verify(repository, times(1)).findByFinishedAtIsNull();
    }
}
