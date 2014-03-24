package com.energyict.mdc.tasks;

import java.util.List;

public interface TaskService {

    String COMPONENT_NAME = "CTS";

    public List<ComTask> getComTasks();

    public ComTask findComTask(long id);
}
