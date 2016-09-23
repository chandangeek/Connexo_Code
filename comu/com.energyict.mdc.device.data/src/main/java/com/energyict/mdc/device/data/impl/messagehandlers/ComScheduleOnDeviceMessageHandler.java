package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.DefaultTranslationKey;
import com.energyict.mdc.device.data.impl.ComScheduleOnDeviceQueueMessage;
import com.energyict.mdc.device.data.impl.MessageSeeds;
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

    public MessageHandler init(DeviceService deviceService, SchedulingService schedulingService, JsonService jsonService, Thesaurus thesaurus) {
        this.deviceService = deviceService;
        this.schedulingService = schedulingService;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
        return this;
    }

    @Override
    public void process(Message message) {
        ComScheduleOnDeviceQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), ComScheduleOnDeviceQueueMessage.class);
        Optional<ComSchedule> comSchedule = schedulingService.findSchedule(queueMessage.comScheduleId);
        if (!comSchedule.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.NO_SUCH_COM_SCHEDULE).format(queueMessage.comScheduleId));
            return;
        }
        Optional<Device> device = deviceService.findDeviceById(queueMessage.deviceId);
        if (!device.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.NO_SUCH_DEVICE).format(queueMessage.deviceId));
            return;
        }

        switch (queueMessage.action) {
            case Add:
                addSchedule(comSchedule.get(), device.get(), queueMessage);
                break;
            case Remove:
                removeSchedule(comSchedule.get(), device.get(), queueMessage);
                break;
            default:
                LOGGER.log(Level.WARNING, "Unknown action for ComSchedule on device: " + queueMessage.action);
        }
    }

    @Override
    public void onMessageDelete(Message message) {
        //do nothing
    }

    private void addSchedule(ComSchedule comSchedule, Device device, ComScheduleOnDeviceQueueMessage queueMessage) {
        try {
            device.newScheduledComTaskExecution(comSchedule).add();
            LOGGER.info(thesaurus.getFormat(DefaultTranslationKey.COM_SCHEDULE_ADDED).format(queueMessage.comScheduleId, device.getName()));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    private void removeSchedule(ComSchedule comSchedule, Device device, ComScheduleOnDeviceQueueMessage queueMessage) {
        try {
            device.removeComSchedule(comSchedule);
            LOGGER.info(thesaurus.getFormat(DefaultTranslationKey.COM_SCHEDULE_REMOVED).format(queueMessage.comScheduleId, device.getName()));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage());
        }
    }
}
