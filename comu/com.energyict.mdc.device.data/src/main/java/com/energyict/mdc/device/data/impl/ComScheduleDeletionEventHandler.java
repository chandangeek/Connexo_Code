package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.model.ComSchedule;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name="com.energyict.mdc.device.data.comschedule.delete.eventhandler", service = Subscriber.class, immediate = true)
public class ComScheduleDeletionEventHandler extends EventHandler<LocalEvent> {

    private static final String TOPIC = com.energyict.mdc.scheduling.events.EventType.COMSCHEDULE_DELETED.topic();

    private volatile DeviceDataService deviceDataService;
    private volatile Thesaurus thesaurus;

    public ComScheduleDeletionEventHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... objects) {
        if (event.getType().getTopic().equals(TOPIC)) {
            this.handleDeleteComSchedule((ComSchedule) event.getSource());
        }
    }

    private void handleDeleteComSchedule(ComSchedule comSchedule) {
        for (ComTaskExecution comTaskExecution : this.deviceDataService.findComTaskExecutionsByComSchedule(comSchedule)) {
            comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution).comSchedule(null).removeNextExecutionSpec().update();
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(DeviceConfigurationService.COMPONENTNAME, Layer.DOMAIN));
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    private void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }


}