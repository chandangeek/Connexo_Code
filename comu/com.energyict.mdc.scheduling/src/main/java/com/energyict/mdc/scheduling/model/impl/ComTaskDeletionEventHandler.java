/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Listens for delete events of {@link ComTask}s
 * and will veto the deletion if the ComTask
 * is still used by at least one {@link ComSchedule}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-22 (08:42)
 */
@Component(name="com.energyict.mdc.scheduling.comtask.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class ComTaskDeletionEventHandler implements TopicHandler {

    private volatile ServerSchedulingService schedulingService;

    // For OSGi purposes
    public ComTaskDeletionEventHandler() {
        super();
    }

    // For unit testing purposes only
    public ComTaskDeletionEventHandler(ServerSchedulingService schedulingService) {
        this.setSchedulingService(schedulingService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ComTask source = (ComTask) localEvent.getSource();
        List<ComSchedule> comSchedules = this.schedulingService.findComSchedulesUsing(source);
        if (!comSchedules.isEmpty()) {
            throw new VetoDeleteComTaskException(getThesaurus(), source, comSchedules);
        }
    }

    private Thesaurus getThesaurus() {
        return this.schedulingService.getThesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/tasks/comtask/VALIDATE_DELETE";
    }

    @Reference
    public void setSchedulingService(ServerSchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

}