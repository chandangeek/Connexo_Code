package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TaskMinInfo {

    public Long id;
    public String application;
    public String name;
    public String type;
    public String displayType;

    public TaskMinInfo() {

    }

    public static TaskMinInfo from(RecurrentTask recurrentTask, Thesaurus thesaurus) {
        TaskMinInfo info = new TaskMinInfo();

        info.id = recurrentTask.getId();
        info.name = recurrentTask.getName();
        info.application = recurrentTask.getApplication();
        info.displayType = thesaurus.getString(recurrentTask.getDestination().getName(), recurrentTask.getDestination().getName());
        return info;
    }

}
