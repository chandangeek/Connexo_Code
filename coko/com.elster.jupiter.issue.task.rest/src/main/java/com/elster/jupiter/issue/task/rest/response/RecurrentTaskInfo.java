/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.rest.response;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class RecurrentTaskInfo {

    public Long id;
    public String name;
    public String application;
    public String queue;
    public String displayType;

    public RecurrentTaskInfo() {

    }

    public static RecurrentTaskInfo from(Thesaurus thesaurus, RecurrentTask recurrentTask) {
        RecurrentTaskInfo info = new RecurrentTaskInfo();
        info.id = recurrentTask.getId();
        info.name = recurrentTask.getName();
        info.application = recurrentTask.getApplication();
        info.queue = recurrentTask.getDestination().getName();
        info.displayType = thesaurus.getString(recurrentTask.getDestination().getName(), recurrentTask.getDestination().getName());
        return info;
    }

}


