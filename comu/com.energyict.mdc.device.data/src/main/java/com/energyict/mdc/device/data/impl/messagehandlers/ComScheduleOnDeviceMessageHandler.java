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
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.ScheduleAddStrategy;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
        Optional<Device> device = deviceService.findByUniqueMrid(queueMessage.mRID);
        if (!device.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.NO_SUCH_DEVICE).format(queueMessage.mRID));
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
            List<ComSchedule> conflictingSchedules = device.getComTaskExecutions().stream()
                    .filter(ComTaskExecution::usesSharedSchedule)
                    .filter(comTaskExecution -> comSchedule.getComTasks().contains(comTaskExecution.getComTask()))
                    .map(comTaskExecution -> (ScheduledComTaskExecution) comTaskExecution)
                    .map(ScheduledComTaskExecution::getComSchedule)
                    .distinct()
                    .collect(Collectors.toList());

            if(conflictingSchedules.size() > 0 && queueMessage.strategy.equals(ScheduleAddStrategy.KEEP_EXISTING)) {
                LOGGER.info(thesaurus.getFormat(DefaultTranslationKey.COM_SCHEDULE_UNABLE_TO_ADD).format(queueMessage.comScheduleId, queueMessage.mRID));
            } else {
                removeExistingSchedulesAndAddNewSchedule(comSchedule, device, queueMessage, conflictingSchedules);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    private void removeExistingSchedulesAndAddNewSchedule(ComSchedule comSchedule, Device device, ComScheduleOnDeviceQueueMessage queueMessage, List<ComSchedule> conflictingSchedules) {
        conflictingSchedules.stream()
                .forEach(device::removeComSchedule);

        device.getComTaskExecutions().stream()
                .filter(ComTaskExecution::isScheduledManually)
                .filter(comTaskExecution -> comSchedule.getComTasks().contains(comTaskExecution.getComTask()))
                .forEach(device::removeComTaskExecution);

        device.newScheduledComTaskExecution(comSchedule).add();

        LOGGER.info(thesaurus.getFormat(DefaultTranslationKey.COM_SCHEDULE_ADDED).format(queueMessage.comScheduleId, queueMessage.mRID));
    }

    private void removeSchedule(ComSchedule comSchedule, Device device, ComScheduleOnDeviceQueueMessage queueMessage) {
        try {
            device.removeComSchedule(comSchedule);
            LOGGER.info(thesaurus.getFormat(DefaultTranslationKey.COM_SCHEDULE_REMOVED).format(queueMessage.comScheduleId, queueMessage.mRID));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage());
        }
    }
}
