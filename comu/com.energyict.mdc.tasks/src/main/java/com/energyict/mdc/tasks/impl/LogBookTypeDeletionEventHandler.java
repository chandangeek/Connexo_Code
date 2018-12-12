/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.tasks.LogBooksTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

/**
 * Listens for delete events that relate to {@link LogBookType}s
 * and will veto the deletion of the LogBookType is still in use
 * by at least one {@link com.energyict.mdc.tasks.LogBooksTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-21 (10:23)
 */
@Component(name="com.energyict.mdc.tasks.delete.logbooktype.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class LogBookTypeDeletionEventHandler implements TopicHandler {

    private volatile ServerTaskService taskService;

    // For OSGi framework only
    public LogBookTypeDeletionEventHandler() {
        super();
    }

    @Inject
    // For unit testing purposes
    public LogBookTypeDeletionEventHandler(ServerTaskService taskService) {
        this();
        this.taskService = taskService;
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/masterdata/logbooktype/VALIDATEDELETE";
    }

    private Thesaurus getThesaurus() {
        return this.taskService.getThesaurus();
    }

    @Override
    public void handle(LocalEvent localEvent) {
        LogBookType logBookType = (LogBookType) localEvent.getSource();
        List<LogBooksTask> logBooksTasks = this.taskService.findTasksUsing(logBookType);
        if (!logBooksTasks.isEmpty()) {
            throw new VetoDeleteLogBookTypeException(this.getThesaurus(), logBookType, logBooksTasks);
        }
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTaskService(ServerTaskService taskService) {
        this.taskService = taskService;
    }

}