package com.energyict.mdc.device.lifecycle.config.bpmhandler;

import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcessStartEvent;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;

import static com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess.DEVICE_ID_BPM_PARAMETER_NAME;
import static com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess.STATE_ID_BPM_PARAMETER_NAME;


/**
 * Handles {@link TransitionBusinessProcessStartEvent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (16:47)
 */
@Component(name="com.energyict.mdc.device.lifecycle.config.bpmhandler", service = TopicHandler.class, immediate = true)
public class TransitionBusinessProcessStartEventTopicHandler implements TopicHandler {

    private volatile BpmService bpmService;

    // For OSGi purposes
    public TransitionBusinessProcessStartEventTopicHandler() {
        super();
    }

    // For testing purposes
    public TransitionBusinessProcessStartEventTopicHandler(BpmService bpmService) {
        this();
        this.setBpmService(bpmService);
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Override
    public String getTopicMatcher() {
        return TransitionBusinessProcessStartEvent.TOPIC;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.handle((TransitionBusinessProcessStartEvent) localEvent.getSource());
    }

    public void handle(TransitionBusinessProcessStartEvent event) {
        Map<String, Object> parameters = ImmutableMap.of(
                DEVICE_ID_BPM_PARAMETER_NAME, event.deviceId(),
                STATE_ID_BPM_PARAMETER_NAME, event.state().getId());
        this.bpmService.startProcess(event.deploymentId(), event.processId(), parameters);
    }

}