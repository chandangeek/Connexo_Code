package com.energyict.mdc.task;

import java.util.List;

public interface TaskService {

    String COMPONENT_NAME = "TSK";

    public List<ComTask> getComTasks();
}
