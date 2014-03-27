package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 10:37
 */
@Component(name="com.energyict.mdc.device.config.protocol.delete.connectiontypepluggableclass.eventhandler", service = Subscriber.class, immediate = true)
public class ConnectionTypePluggableClassDeletionEventHandler implements TopicHandler {

    private volatile DeviceConfigurationService deviceConfigurationService;

    @Override
    public void handle(LocalEvent localEvent) {
        ConnectionTypePluggableClass source = (ConnectionTypePluggableClass) localEvent.getSource();
        List<PartialConnectionTask> found = deviceConfigurationService.findByConnectionTypePluggableClass(source);
        if (!found.isEmpty()) {
            throw new VetoDeleteConnectionTypePluggableClassException(getThesaurus(), found);
        }
    }

    private Thesaurus getThesaurus() {
        return ((DeviceConfigurationServiceImpl) deviceConfigurationService).getThesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/protocol/pluggable/connectiontype/DELETED";
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }
}
