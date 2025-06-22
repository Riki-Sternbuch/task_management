package com.example.task_management.service;

import com.example.task_management.model.Task;
import com.example.task_management.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService implements ITaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    //add and update task
    @Override
    public Task saveTask(Task task) {
        if (task.getId() != null) {
            Optional<Task> existingTaskOptional = taskRepository.findById(task.getId());
            if (existingTaskOptional.isPresent()) {
                Task existingTask = existingTaskOptional.get();
                task.setCreatedAt(existingTask.getCreatedAt());

                if (task.isCompleted()) {
                    if (existingTask.getCompletedAt() == null) {
                        task.setCompletedAt(LocalDateTime.now());
                    } else {
                        task.setCompletedAt(existingTask.getCompletedAt());
                    }
                } else {
                    task.setCompletedAt(null);
                }
            } else {
                if (task.isCompleted()) {
                    task.setCompletedAt(LocalDateTime.now());
                } else {
                    task.setCompletedAt(null);
                }
            }
        } else {
            if (task.isCompleted()) {
                task.setCompletedAt(LocalDateTime.now());
            } else {
                task.setCompletedAt(null);
            }
        }
        return taskRepository.save(task);
    }
    //get task by id
    @Override
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    //select all tasks
    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    //mark as completed
    @Override
    public boolean markTaskAsComplete(Long id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            if (!task.isCompleted()) {
                task.setCompleted(true);
                task.setCompletedAt(LocalDateTime.now());
                taskRepository.saveAndFlush(task);
                return true;
            }
        }
        return false;
    }

    //mark as incopmpleted
    @Override
    public boolean markTaskAsIncomplete(Long id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            if (task.isCompleted()) {
                task.setCompleted(false);
                task.setCompletedAt(null);
                taskRepository.saveAndFlush(task);
                return true;
            }
        }
        return false;
    }

    //delete by id
    @Override
    public boolean deleteTaskById(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    //in progres tasks
    @Override
    public List<Task> getPendingTasks() {
        return taskRepository.findByCompleted(false);
    }

    //completed tasks
    @Override
    public List<Task> getCompletedTasks() {
        return taskRepository.findByCompleted(true);
    }

    //search
    @Override
    public List<Task> searchTasks(String query) {
        return taskRepository.findByNameContainingIgnoreCase(query);
    }
}