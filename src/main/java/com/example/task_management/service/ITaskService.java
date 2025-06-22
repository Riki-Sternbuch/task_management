package com.example.task_management.service;

import com.example.task_management.model.Task;
import java.util.List;
import java.util.Optional;

public interface ITaskService {
    Task saveTask(Task task);
    Optional<Task> getTaskById(Long id);
    List<Task> getAllTasks();
    boolean markTaskAsComplete(Long id);
    boolean markTaskAsIncomplete(Long id);
    boolean deleteTaskById(Long id);
    List<Task> getPendingTasks();
    List<Task> getCompletedTasks();
    List<Task> searchTasks(String query);
}