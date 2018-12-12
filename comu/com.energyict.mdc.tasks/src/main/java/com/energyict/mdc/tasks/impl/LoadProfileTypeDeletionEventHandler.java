/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.tasks.LoadProfilesTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name="com.energyict.mdc.tasks.delete.loadprofile.eventhandler", service = TopicHandler.class, immediate = true)
public class LoadProfileTypeDeletionEventHandler implements TopicHandler {
    private static final String TOPIC = "com/energyict/mdc/masterdata/loadprofiletype/VALIDATEDELETE";

    private volatile ServerTaskService taskService;

    public LoadProfileTypeDeletionEventHandler() {
        super();
    }

    @Inject
    public LoadProfileTypeDeletionEventHandler(ServerTaskService taskService) {
        this();
        this.taskService = taskService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    private Thesaurus getThesaurus() {
        return this.taskService.getThesaurus();
    }

    @Override
    public void handle(LocalEvent event) {
        LoadProfileType loadProfileType = (LoadProfileType) event.getSource();
        this.validateDelete(loadProfileType);
    }

    private void validateDelete(LoadProfileType loadProfileType) {
        List<LoadProfilesTask> loadProfileTasks = taskService.findTasksUsing(loadProfileType);
        if (!loadProfileTasks.isEmpty()) {
            throw new VetoDeleteLoadProfileTypeException(this.getThesaurus(), loadProfileType, loadProfileTasks);
        }
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTaskService(ServerTaskService taskService) {
        this.taskService = taskService;
    }

}