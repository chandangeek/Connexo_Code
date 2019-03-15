/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.autoreschedule.impl;

import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.tasks.ComTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens for delete events of {@link ComTask}s
 * and will veto the deletion if the ComTask
 * is still used by at least one {@link com.elster.jupiter.customtask.CustomTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-21 (16:26)
 */
@Component(name="com.energyict.mdc.autoreschedule.delete.comtask.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class ComTaskDeletionEventHandler implements TopicHandler {

    private volatile CustomTaskService customTaskService;
    private volatile Thesaurus thesaurus;

    // For OSGi purposes
    public ComTaskDeletionEventHandler() {
        super();
    }

    // For testing purposes only
    public ComTaskDeletionEventHandler(CustomTaskService customTaskService, NlsService nlsService) {
        this();
        this.setCustomTaskService(customTaskService);
        setThesaurus(nlsService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ComTask source = (ComTask) localEvent.getSource();
        if (isInUse(source)) {
            throw new VetoDeleteComTaskException(thesaurus, source);
        }
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/tasks/comtask/VALIDATE_DELETE";
    }

    @Reference
    @SuppressWarnings("unused")
    public void setCustomTaskService(CustomTaskService customTaskService) {
        this.customTaskService = customTaskService;
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN);
    }

    private boolean isInUse(ComTask comTask) {
        return !customTaskService.findPropertyByComTaskId(comTask.getId(), 0, 1).isEmpty();
    }
}
