package com.elster.jupiter.fsm.handler;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.StateChangeBusinessProcessStartEvent;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;

import static com.elster.jupiter.fsm.StateChangeBusinessProcess.CHANGE_TYPE_BPM_PARAMETER_NAME;
import static com.elster.jupiter.fsm.StateChangeBusinessProcess.SOURCE_ID_BPM_PARAMETER_NAME;
import static com.elster.jupiter.fsm.StateChangeBusinessProcess.STATE_ID_BPM_PARAMETER_NAME;

/**
 * Handles {@link StateChangeBusinessProcessStartEvent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (11:23)
 */
@Component(name="com.elster.jupiter.fsm.handler", service = TopicHandler.class, immediate = true)
public class StateChangeBusinessProcessStartEventTopicHandler implements TopicHandler {

    private volatile BpmService bpmService;

    // For OSGi purposes
    public StateChangeBusinessProcessStartEventTopicHandler() {
        super();
    }

    // For testing purposes
    public StateChangeBusinessProcessStartEventTopicHandler(BpmService bpmService) {
        this();
        this.setBpmService(bpmService);
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Override
    public String getTopicMatcher() {
        return StateChangeBusinessProcessStartEvent.TOPIC;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.handle((StateChangeBusinessProcessStartEvent) localEvent.getSource());
    }

    public void handle(StateChangeBusinessProcessStartEvent event) {
        Map<String, Object> parameters = ImmutableMap.of(
                SOURCE_ID_BPM_PARAMETER_NAME, event.sourceId(),
                STATE_ID_BPM_PARAMETER_NAME, event.state().getId(),
                CHANGE_TYPE_BPM_PARAMETER_NAME, event.type().parameterValue());
        this.bpmService.startProcess(event.deploymentId(), event.processId(), parameters);
    }

}