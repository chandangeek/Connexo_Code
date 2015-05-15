package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.energyict.mdc.firmware.FirmwareService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.firmware.campaigns.handler",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + FirmwareCampaignsHandlerFactory.FIRMWARE_CAMPAIGNS_SUBSCRIBER, "destination=" + FirmwareCampaignsHandlerFactory.FIRMWARE_CAMPAIGNS_DESTINATION},
        immediate = true)
public class FirmwareCampaignsHandlerFactory implements MessageHandlerFactory {
    public static final String FIRMWARE_CAMPAIGNS_DESTINATION = "FirmwareCampaignsQueue";
    public static final String FIRMWARE_CAMPAIGNS_SUBSCRIBER = "FirmwareCampaignsSubscriber";
    public static final String FIRMWARE_CAMPAIGNS_TASK = "FirmwareCampaignsTask";
    public static final ScheduleExpression FIRMWARE_CAMPAIGNS_SCHEDULE_EXPRESSION = PeriodicalScheduleExpression.every(2).minutes().at(0).build();

    private volatile FirmwareService firmwareService;
    private volatile TaskService taskService;

    // OSGI
    @SuppressWarnings("unused")
    public FirmwareCampaignsHandlerFactory() {}

    @Inject
    public FirmwareCampaignsHandlerFactory(TaskService taskService, FirmwareService firmwareService) {
        setTaskService(taskService);
        setFirmwareService(firmwareService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new FirmwareCampaignExecutor((FirmwareServiceImpl) firmwareService));
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
}
