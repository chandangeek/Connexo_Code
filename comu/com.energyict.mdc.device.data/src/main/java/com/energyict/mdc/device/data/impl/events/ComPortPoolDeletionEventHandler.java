package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.ServerDeviceDataService;
import com.energyict.mdc.engine.model.ComPortPool;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 10:37
 */
@Component(name="com.energyict.mdc.device.config.delete.comportpool.eventhandler", service = TopicHandler.class, immediate = true)
public class ComPortPoolDeletionEventHandler implements TopicHandler {

    private volatile ServerDeviceDataService deviceDataService;

    @Override
    public void handle(LocalEvent localEvent) {
        ComPortPool source = (ComPortPool) localEvent.getSource();
        if (this.deviceDataService.hasConnectionTasks(source)) {
            throw new VetoDeleteComPortPoolException(getThesaurus(), source);
        }
    }

    private Thesaurus getThesaurus() {
        return this.deviceDataService.getThesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/engine/config/comportpool/VALIDATE_DELETE";
    }

    @Reference
    public void setDeviceDataService(ServerDeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

}