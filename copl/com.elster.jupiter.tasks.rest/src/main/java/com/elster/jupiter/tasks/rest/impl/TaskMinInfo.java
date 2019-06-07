/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class TaskMinInfo {

    public Long id;
    public String name;
    public String application;
    public String queue;
    public String type;
    public String displayType;

    public TaskMinInfo() {

    }

    public static TaskMinInfo from(Thesaurus thesaurus, RecurrentTask recurrentTask) {
        TaskMinInfo info = new TaskMinInfo();

        info.id = recurrentTask.getId();
        info.name = recurrentTask.getName();
        info.application = recurrentTask.getApplication();
        info.queue = recurrentTask.getDestination().getName();
        info.type = recurrentTask.getDestination().getQueueTypeName();
        info.displayType = thesaurus.getString(info.type, info.type);
        return info;
    }

}


