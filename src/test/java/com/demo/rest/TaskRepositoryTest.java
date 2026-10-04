package com.demo.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskRepositoryTest {

    private TaskRepository repo;

    @BeforeEach
    void setUp() {
        repo = new TaskRepository();
    }

    @Test
    void createAssignsIncreasingIds() {
        Task a = repo.create(new Task("a", false));
        Task b = repo.create(new Task("b", true));
        assertEquals(1L, a.getId());
        assertEquals(2L, b.getId());
        assertEquals("b", b.getTitle());
        assertTrue(b.isDone());
    }

    @Test
    void createIgnoresClientSuppliedId() {
        Task input = new Task("a", false);
        input.setId(99L);
        Task created = repo.create(input);
        assertEquals(1L, created.getId());
        assertTrue(repo.findById(99L).isEmpty());
    }

    @Test
    void findByIdReturnsExistingTask() {
        Task created = repo.create(new Task("a", false));
        assertEquals("a", repo.findById(created.getId()).orElseThrow().getTitle());
    }

    @Test
    void findByIdReturnsEmptyForMissingId() {
        assertTrue(repo.findById(42L).isEmpty());
    }

    @Test
    void updateChangesExistingTaskAndKeepsPathId() {
        Task created = repo.create(new Task("a", false));
        Task changes = new Task("b", true);
        changes.setId(99L);

        Task updated = repo.update(created.getId(), changes).orElseThrow();

        assertEquals(created.getId(), updated.getId());
        assertEquals("b", updated.getTitle());
        assertTrue(updated.isDone());
        assertEquals("b", repo.findById(created.getId()).orElseThrow().getTitle());
        assertTrue(repo.findById(99L).isEmpty());
    }

    @Test
    void updateReturnsEmptyForMissingId() {
        assertTrue(repo.update(42L, new Task("b", true)).isEmpty());
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void deleteRemovesExistingTask() {
        Task created = repo.create(new Task("a", false));
        assertTrue(repo.delete(created.getId()));
        assertTrue(repo.findById(created.getId()).isEmpty());
    }

    @Test
    void deleteReturnsFalseForMissingId() {
        assertFalse(repo.delete(42L));
    }

    @Test
    void findAllIsSortedByIdAscending() {
        for (int i = 0; i < 20; i++) {
            repo.create(new Task("t" + i, false));
        }
        repo.delete(5L);
        List<Long> ids = repo.findAll().stream().map(Task::getId).toList();
        assertEquals(ids.stream().sorted().toList(), ids);
        assertEquals(19, ids.size());
    }

    @Test
    void concurrentCreatesGetUniqueIds() throws InterruptedException {
        int count = 1000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < count; i++) {
            pool.submit(() -> ids.add(repo.create(new Task("t", false)).getId()));
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(count, ids.size());
        assertEquals(count, repo.findAll().size());
        assertEquals(new HashSet<>(ids), new HashSet<>(repo.findAll().stream().map(Task::getId).toList()));
    }
}
