package br.edu.ifpr.todolistif.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpr.todolistif.model.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByFinishedAtIsNotNull();

    List<Todo> findByFinishedAtIsNull();
}
