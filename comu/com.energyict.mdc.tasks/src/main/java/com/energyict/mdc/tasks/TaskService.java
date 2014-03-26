package com.energyict.mdc.tasks;

import java.util.List;

public interface TaskService {

    String COMPONENT_NAME = "CTS";

    public ComTask createComTask();

    public List<ComTask> getComTasks();

    public ComTask findComTask(long id);

    public List<ComTask> findAllComTasks();
}
