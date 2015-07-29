package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.tasks.RegistersTask;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

/**
 * Responds to validate delete events sent by {@link RegisterGroup}s
 * and will veto the delete when at least one
 * {@link com.energyict.mdc.tasks.ComTask} is still using it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-13 (10:51)
 */
@Component(name = "com.energyict.mdc.tasks.registergroup.delete.handler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class RegisterGroupDeletionEventHandler implements TopicHandler {

    private static final String TOPIC_NAME = "com/energyict/mdc/masterdata/registergroup/VALIDATEDELETE";

    private volatile ServerTaskService taskService;

    // For OSGi purposes
    public RegisterGroupDeletionEventHandler() {
        super();
    }

    // For testing purposes
    @Inject
    public RegisterGroupDeletionEventHandler(ServerTaskService taskService) {
        this();
        this.setTaskService(taskService);
    }

    @Reference
    public void setTaskService(ServerTaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC_NAME;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.handle((RegisterGroup) localEvent.getSource());
    }

    private void handle(RegisterGroup registerGroup) {
        List<RegistersTask> registersTasks = taskService.findTasksUsing(registerGroup);
        if (!registersTasks.isEmpty()) {
            throw new VetoDeleteRegisterGroupException(this.getThesaurus(), registerGroup, registersTasks);
        }
    }

    private Thesaurus getThesaurus() {
        return this.taskService.getThesaurus();
    }

}