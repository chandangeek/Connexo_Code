package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import org.osgi.service.component.annotations.Component;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 10:37
 */
@Component(name = "com.energyict.mdc.device.data.delete.devicegroup.eventhandler", service = TopicHandler.class, immediate = true)
public class DeviceGroupDeletionEventHandler implements TopicHandler {

    @Override
    public void handle(LocalEvent localEvent) {
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/engine/config/devicegroup/VALIDATE_DELETE";
    }


}