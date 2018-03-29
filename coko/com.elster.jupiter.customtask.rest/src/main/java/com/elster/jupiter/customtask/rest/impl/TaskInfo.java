/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.tasks.RecurrentTask;

public class TaskInfo {

    public Long id;
    public String name;
    public String application;
    public String type;

    public TaskInfo() {

    }

    public static TaskInfo from(RecurrentTask recurrentTask) {
        TaskInfo info = new TaskInfo();

        info.id = recurrentTask.getId();
        info.name = recurrentTask.getName();
        info.application = recurrentTask.getApplication();
        info.type = "";

        return info;
    }

}
