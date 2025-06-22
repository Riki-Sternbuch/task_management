package com.example.task_management.controller;

import com.example.task_management.service.ITaskService;
import org.springframework.ui.Model;
import com.example.task_management.model.Task;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    private final ITaskService taskService;

    public TaskController(ITaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public String getAllTasks(
            Model model,
            @RequestParam(required = false, defaultValue = "all") String filter,
            @RequestParam(required = false) String query
    ) {
        List<Task> tasks;
        String activeFilter = filter;
        boolean searchResults = false;

        if (query != null && !query.trim().isEmpty()) {
            tasks = taskService.searchTasks(query);
            searchResults = true;
            model.addAttribute("query", query);
        } else {
            switch (filter) {
                case "completed":
                    tasks = taskService.getCompletedTasks();
                    break;
                case "pending":
                    tasks = taskService.getPendingTasks();
                    break;
                case "all":
                default:
                    tasks = taskService.getAllTasks();
                    break;
            }
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("activeFilter", activeFilter);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("newTask", new Task());

        return "TaskList";
    }

    @GetMapping("/new")
    public String showNewTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("isEdit", false);
        return "TaskForm";
    }


    @GetMapping("/edit/{id}")
    public String showEditTaskForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Task> task = taskService.getTaskById(id);
        if (task.isPresent()) {
            model.addAttribute("task", task.get());
            model.addAttribute("isEdit", true);
            return "TaskForm";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Task not found!");
            return "redirect:/tasks";
        }
    }


    @PostMapping("/save")
    public String saveTask(@Valid @ModelAttribute("task") Task task,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", task.getId() != null);
            return "TaskForm";
        }

        taskService.saveTask(task);
        redirectAttributes.addFlashAttribute("successMessage", "Task saved successfully!");
        return "redirect:/tasks";
    }

    @PostMapping("/complete/{id}")
    public String completeTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (taskService.markTaskAsComplete(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Task marked as complete!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Task not found or already complete.");
        }
        return "redirect:/tasks";
    }

    @PostMapping("/incomplete/{id}")
    public String incompleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (taskService.markTaskAsIncomplete(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Task marked as incomplete!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Task not found or already incomplete.");
        }
        return "redirect:/tasks";
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (taskService.deleteTaskById(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Task deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Task not found or could not be deleted.");
        }
        return "redirect:/tasks";
    }


    @GetMapping("/api/statistics")
    @ResponseBody
    public Object getStatistics() {
        long totalTasks = taskService.getAllTasks().size();
        long completedTasks = taskService.getCompletedTasks().size();
        long pendingTasks = totalTasks - completedTasks;
        double completionPercentage = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

        String productivityMessage;
        if (completionPercentage == 100) {
            productivityMessage = "Excellent! All tasks completed";
        } else if (completionPercentage > 75) {
            productivityMessage = "You're on the right track! Just a bit more";
        } else if (completionPercentage > 0) {
            productivityMessage = "Start working! You have open tasks";
        } else {
            productivityMessage = "Start adding new tasks!";
        }

        return new Object() {
            public long getTotalTasks() { return totalTasks; }
            public long getCompletedTasks() { return completedTasks; }
            public long getPendingTasks() { return pendingTasks; }
            public double getCompletionPercentage() { return Math.round(completionPercentage * 100.0) / 100.0; }
            public String getProductivityMessage() { return productivityMessage; }
        };
    }
}