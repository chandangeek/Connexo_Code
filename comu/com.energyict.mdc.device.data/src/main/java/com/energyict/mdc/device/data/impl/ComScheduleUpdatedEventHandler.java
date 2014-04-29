package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.model.ComSchedule;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name="com.energyict.mdc.device.data.comschedule.update.messagehandler", service = MessageHandler.class, immediate = true)
public class ComScheduleUpdatedEventHandler implements MessageHandler {

    private static final String TOPIC = com.energyict.mdc.scheduling.events.EventType.COMSCHEDULES_UPDATED.topic();

    private volatile DeviceDataService deviceDataService;

    public ComScheduleUpdatedEventHandler() {
        super();
    }

    @Override
    public void process(Message message) {
    }

    private void handleUpdatedComSchedule(ComSchedule comSchedule) {
        for (ComTaskExecution comTaskExecution : this.deviceDataService.findComTaskExecutionsByComSchedule(comSchedule)) {
            comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution).comSchedule(null).removeNextExecutionSpec().update();
        }
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }


}