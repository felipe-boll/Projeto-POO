package br.edu.ifpr.todolistif.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.edu.ifpr.todolistif.dto.TodoConclusaoDTO;
import br.edu.ifpr.todolistif.dto.TodoCreateDTO;
import br.edu.ifpr.todolistif.dto.TodoResponseDTO;
import br.edu.ifpr.todolistif.model.Todo;
import br.edu.ifpr.todolistif.service.TodoService;

@ExtendWith(MockitoExtension.class)
public class TodoApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TodoService service;

    @InjectMocks
    private TodoApiController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Todo criarTodoExemplo() {
        Todo todo = new Todo();
        todo.setId(1L);
        todo.setTitle("Estudar Spring Boot");
        todo.setDeadLine(LocalDateTime.of(2026, 10, 30, 18, 0, 0));
        return todo;
    }

    @Test
    void deveRetornarStatus201AoCriarTarefa() throws Exception {
        Todo todo = criarTodoExemplo();
        TodoResponseDTO responseDTO = new TodoResponseDTO(todo);

        when(service.criar(any(TodoCreateDTO.class))).thenReturn(responseDTO);

        String jsonEnviado = """
                {
                    "title": "Estudar Spring Boot",
                    "deadLine": "2026-10-30T18:00:00"
                }
                """;

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEnviado))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Estudar Spring Boot"))
                .andExpect(jsonPath("$.finishedAt").doesNotExist());

        verify(service, times(1)).criar(any(TodoCreateDTO.class));
    }

    @Test
    void deveRetornarStatus204AoExcluirTarefa() throws Exception {
        doNothing().when(service).excluir(1L);

        mockMvc.perform(delete("/api/todos/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).excluir(1L);
    }

    @Test
    void deveRetornarStatus200AoInformarConclusao() throws Exception {
        Todo todo = criarTodoExemplo();
        LocalDateTime conclusao = LocalDateTime.of(2026, 10, 22, 10, 30, 0);
        todo.setFinishedAt(conclusao);
        TodoResponseDTO responseDTO = new TodoResponseDTO(todo);

        when(service.marcarConclusao(eq(1L), any(TodoConclusaoDTO.class))).thenReturn(responseDTO);

        String jsonEnviado = """
                {
                    "finishedAt": "2026-10-22T10:30:00"
                }
                """;

        mockMvc.perform(patch("/api/todos/1/conclusao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonEnviado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.finishedAt").value("2026-10-22T10:30:00"));

        verify(service, times(1)).marcarConclusao(eq(1L), any(TodoConclusaoDTO.class));
    }

    @Test
    void deveListarConcluidasComStatus200() throws Exception {
        Todo todo = criarTodoExemplo();
        todo.setFinishedAt(LocalDateTime.of(2026, 10, 22, 10, 30, 0));
        TodoResponseDTO responseDTO = new TodoResponseDTO(todo);

        when(service.listarConcluidas()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/todos/concluidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].finishedAt").value("2026-10-22T10:30:00"));

        verify(service, times(1)).listarConcluidas();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverConcluidas() throws Exception {
        when(service.listarConcluidas()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/todos/concluidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(service, times(1)).listarConcluidas();
    }

    @Test
    void deveListarPendentesComStatus200() throws Exception {
        Todo todo = criarTodoExemplo();
        TodoResponseDTO responseDTO = new TodoResponseDTO(todo);

        when(service.listarPendentes()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/todos/pendentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].finishedAt").doesNotExist());

        verify(service, times(1)).listarPendentes();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverPendentes() throws Exception {
        when(service.listarPendentes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/todos/pendentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(service, times(1)).listarPendentes();
    }

    @Test
    void deveListarTodasComStatus200() throws Exception {
        Todo todo = criarTodoExemplo();
        TodoResponseDTO responseDTO = new TodoResponseDTO(todo);

        when(service.listar()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(service, times(1)).listar();
    }
}
