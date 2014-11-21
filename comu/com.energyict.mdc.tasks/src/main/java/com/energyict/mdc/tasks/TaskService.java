package com.energyict.mdc.tasks;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    String COMPONENT_NAME = "CTS";

    public ComTask newComTask(String name);

    public Optional<ComTask> findComTask(long id);

    public List<ComTask> findAllComTasks();

    public Optional<ProtocolTask> findProtocolTask(long id);

}