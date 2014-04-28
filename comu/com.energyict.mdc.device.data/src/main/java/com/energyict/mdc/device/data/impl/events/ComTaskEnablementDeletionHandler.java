package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.pubsub.EventHandler;
import com.energyict.mdc.device.config.ComTaskEnablement;
import org.osgi.service.component.annotations.Component;

/**
 * Handles delete events that are being sent when a {@link ComTaskEnablement}
 * is about to be deleted and will veto the delete when it is in use by at least one device.
 * Todo (JP-1125): complete implementation as part of the port of ComTaskExecution to the new ORM framework
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (11:51)
 */
@Component(name="com.energyict.mdc.device.data.delete.comtaskenablement.eventhandler", service = TopicHandler.class, immediate = true)
public class ComTaskEnablementDeletionHandler extends EventHandler<LocalEvent> {

    private static final String TOPIC = "com/energyict/mdc/device/config/comtaskenablement/VALIDATEDELETE";

    protected ComTaskEnablementDeletionHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(TOPIC)) {
            ComTaskEnablement comTaskEnablement = (ComTaskEnablement) event.getSource();
            this.validateNotUsedByDevice(comTaskEnablement);
        }
    }

    /**
     * Vetos the delection of the {@link ComTaskEnablement}
     * by throwing an exception when the ComTaskEnablement
     * is used by at least on Device, i.e. at least one
     * ComTaskExecution that uses it on a Device.
     *
     * @param comTaskEnablement The ComTaskEnablement that is about to be deleted
     */
    private void validateNotUsedByDevice(ComTaskEnablement comTaskEnablement) {
        return;
    }

}