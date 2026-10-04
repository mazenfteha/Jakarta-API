package com.demo.rest;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TaskRepository {

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public List<Task> findAll() {
        return tasks.values().stream()
                .sorted(Comparator.comparing(Task::getId))
                .toList();
    }

    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public Task create(Task task) {
        Task stored = new Task(task.getTitle(), task.isDone());
        stored.setId(nextId.getAndIncrement());
        tasks.put(stored.getId(), stored);
        return stored;
    }

    public Optional<Task> update(Long id, Task task) {
        return Optional.ofNullable(tasks.computeIfPresent(id, (key, existing) -> {
            Task replaced = new Task(task.getTitle(), task.isDone());
            replaced.setId(key);
            return replaced;
        }));
    }

    public boolean delete(Long id) {
        return tasks.remove(id) != null;
    }
}
