package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.engine.model.ComPortPool;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 10:37
 */
@Component(name="com.energyict.mdc.device.data.delete.comportpool.eventhandler", service = TopicHandler.class, immediate = true)
public class ComPortPoolDeletionEventHandler implements TopicHandler {

    private volatile ServerConnectionTaskService connectionTaskService;
    private volatile DeviceDataModelService deviceDataModelService;

    @Override
    public void handle(LocalEvent localEvent) {
        ComPortPool source = (ComPortPool) localEvent.getSource();
        if (this.connectionTaskService.hasConnectionTasks(source)) {
            throw new VetoDeleteComPortPoolException(getThesaurus(), source);
        }
    }

    private Thesaurus getThesaurus() {
        return this.deviceDataModelService.thesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/engine/config/comportpool/VALIDATE_DELETE";
    }

    @Reference
    public void setConnectionTaskService(ServerConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

}