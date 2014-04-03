package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.engine.model.ComPortPool;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 10:37
 */
@Component(name="com.energyict.mdc.device.config.delete.comportpool.eventhandler", service = Subscriber.class, immediate = true)
public class ComPortPoolDeletionEventHandler implements TopicHandler {

    private volatile DeviceConfigurationService deviceConfigurationService;

    @Override
    public void handle(LocalEvent localEvent) {
        ComPortPool source = (ComPortPool) localEvent.getSource();
        List<ServerPartialConnectionTask> found = deviceConfigurationService.findByComPortPool(source);
        if (!found.isEmpty()) {
            throw new VetoDeleteComPortPoolException(getThesaurus(), source, found);
        }
    }

    private Thesaurus getThesaurus() {
        return ((DeviceConfigurationServiceImpl) deviceConfigurationService).getThesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/engine/model/comportpool/DELETED";
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }
}
