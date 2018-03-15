package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.tasks.RecurrentTask;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TaskMinInfo {

    public Long id;
    public String name;
    public String application;

    public TaskMinInfo() {

    }

    public static TaskMinInfo from(RecurrentTask recurrentTask) {
        TaskMinInfo info = new TaskMinInfo();

        info.id = recurrentTask.getId();
        info.name = recurrentTask.getName();
        info.application = recurrentTask.getApplication();
        return info;
    }

}