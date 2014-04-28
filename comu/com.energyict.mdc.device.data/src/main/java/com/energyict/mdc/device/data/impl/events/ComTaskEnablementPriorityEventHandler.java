package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.pubsub.EventHandler;
import com.energyict.mdc.device.config.ComTaskEnablement;
import org.osgi.service.component.annotations.Component;

/**
 * Handles events that are being sent when the priority of a {@link ComTaskEnablement} changes.
 * Todo (JP-1125): complete implementation as part of the port of ComTaskExecution to the new ORM framework
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (11:51)
 */
@Component(name="com.energyict.mdc.device.data.delete.comtaskenablement.eventhandler", service = TopicHandler.class, immediate = true)
public class ComTaskEnablementPriorityEventHandler extends EventHandler<LocalEvent> {

    private static final String TOPIC = "com/energyict/mdc/device/config/comtaskenablement/PRIORITY_UPDATED";

    protected ComTaskEnablementPriorityEventHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(TOPIC)) {
            // Code from 9.1
//            this.getComTaskExecutionFactory().
//                    preferredPriorityChanged(
//                            this.getComTask(),
//                            this.getDeviceCommunicationConfiguration().getDeviceConfiguration(),
//                            previousPriority,
//                            shadow.getPriority());
        }
    }

}