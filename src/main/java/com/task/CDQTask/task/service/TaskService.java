package com.task.CDQTask.task.service;

import com.task.CDQTask.task.dto.TaskAddRecord;
import com.task.CDQTask.task.model.Task;
import com.task.CDQTask.task.repository.TaskRepository;
import com.task.CDQTask.task.utils.Config;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    public static final Logger LOGGER = Logger.getLogger(TaskService.class.getName());
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        LOGGER.info("Create TaskService with auto injected: " + taskRepository.getClass().getName());
        this.taskRepository = taskRepository;
    }


    public Task createTask(TaskAddRecord taskRecord) {
        LOGGER.info("createTask taskRecord: " + taskRecord);
        var task = new Task();
        task.setBase(taskRecord.getBase());
        task.setExponent(taskRecord.getExponent());
        task.setStatus(Config.STATUS_RUNNING);
        task = taskRepository.save(task);
        return task;
    }

    public Task getTask(Long id) {
        LOGGER.info("getTask id: " + id);
        return taskRepository.findById(id).orElse(null);
    }

    public List<Task> getAllTasks() {
        LOGGER.info("getAllTasks: ");
        return taskRepository.findAll();
    }

    @Async
    public void processTask(Task task) {
        BigDecimal currentResult = BigDecimal.valueOf(1);
        for (int i = 0; i < Math.abs(task.getExponent()); i++) {
            if (task.getExponent() >= 0) {
                currentResult = currentResult.multiply(BigDecimal.valueOf(task.getBase()));
            } else {
                currentResult = currentResult.divide(BigDecimal.valueOf(task.getBase()));
            }
            task.setProgress(calculateProgress(i, task.getExponent()));
            taskRepository.save(task);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        task.setStatus(Config.STATUS_FINISHED);
        task.setResult(currentResult);
        task.setProgress(100);
        taskRepository.save(task);
    }

    private int calculateProgress(int currentStep, int allSteps) {
        return Math.round((100f / allSteps) * currentStep);
    }


}
