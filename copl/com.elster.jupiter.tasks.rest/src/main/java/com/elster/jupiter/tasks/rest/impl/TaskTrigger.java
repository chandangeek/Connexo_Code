/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;

import java.util.List;
import java.util.stream.Collectors;

public class TaskTrigger {

    public TaskMinInfo recurrentTask;
    public List<TaskMinInfo> nextRecurrentTasks;
    public List<TaskMinInfo> previousRecurrentTasks;

    public TaskTrigger() {

    }

    public static TaskTrigger from(Thesaurus thesaurus, RecurrentTask rt) {
        TaskTrigger taskTrigger = new TaskTrigger();

        taskTrigger.recurrentTask = new TaskMinInfo();
        taskTrigger.recurrentTask.id = rt.getId();
        taskTrigger.recurrentTask.name = rt.getName();
        taskTrigger.recurrentTask.application = rt.getApplication();
        taskTrigger.recurrentTask.queue = rt.getDestination().getName();
        taskTrigger.nextRecurrentTasks = rt.getNextRecurrentTasks().stream()
                .map(recurrentTask -> TaskMinInfo.from(thesaurus, recurrentTask)).collect(Collectors.toList());

        taskTrigger.previousRecurrentTasks = rt.getPrevRecurrentTasks().stream()
                .map(recurrentTask -> TaskMinInfo.from(thesaurus, recurrentTask)).collect(Collectors.toList());

        return taskTrigger;
    }
}