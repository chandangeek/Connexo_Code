package com.elster.jupiter.demo.impl.finders;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;

public class ComTaskFinder extends NamedFinder<ComTaskFinder, ComTask> {
    private final TaskService taskService;

    @Inject
    public ComTaskFinder(TaskService taskService) {
        super(ComTaskFinder.class);
        this.taskService = taskService;
    }

    @Override
    public ComTask find() {
        return taskService.findAllComTasks().stream().filter(ct -> ct.getName().equals(getName())).findFirst().orElseThrow(getFindException());
    }
}
