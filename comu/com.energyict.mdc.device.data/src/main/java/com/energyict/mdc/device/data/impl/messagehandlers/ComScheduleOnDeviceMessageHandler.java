package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ComScheduleOnDeviceQueueMessage;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.RescheduleConnectionTaskQueueMessage;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This message handler will add/remove a single com schedule to/from a single device
 */
public class ComScheduleOnDeviceMessageHandler implements MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(ComScheduleOnDeviceMessageHandler.class.getSimpleName());

    private DeviceService deviceService;
    private SchedulingService schedulingService;
    private JsonService jsonService;
    private Thesaurus thesaurus;

    @Override
    public void process(Message message) {
        ComScheduleOnDeviceQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), ComScheduleOnDeviceQueueMessage.class);
        Optional<ComSchedule> comSchedule = schedulingService.findSchedule(queueMessage.comScheduleId);
        if (!comSchedule.isPresent()) {
            LOGGER.log(Level.SEVERE, "No ComSchedule with id "+queueMessage.comScheduleId);
            return ;
        }
        Optional<Device> device = deviceService.findByUniqueMrid(queueMessage.mRID);
        if (!device.isPresent()) {
            LOGGER.log(Level.SEVERE, "No device mRID "+queueMessage.mRID);
            return ;
        }

        switch (queueMessage.action) {
            case Add: device.get().newScheduledComTaskExecution(comSchedule.get());
                break;
            case Remove: device.get().removeComSchedule(comSchedule.get());
                break;
            default: LOGGER.log(Level.WARNING, "Unknown action for ComSchedule on device: "+ queueMessage.action);
        }
    }


    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(DeviceService deviceService, SchedulingService schedulingService, JsonService jsonService, Thesaurus thesaurus) {
        this.deviceService = deviceService;
        this.schedulingService = schedulingService;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
        return this;
    }
}
