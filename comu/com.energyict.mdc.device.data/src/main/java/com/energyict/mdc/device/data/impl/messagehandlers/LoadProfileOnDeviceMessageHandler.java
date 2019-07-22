package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonDeserializeException;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadProfileOnDeviceMessageHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(LoadProfileOnDeviceMessageHandler.class.getSimpleName());
    private DeviceService deviceService;
    private JsonService jsonService;
    private Thesaurus thesaurus;

    public MessageHandler init(DeviceService deviceService, JsonService jsonService, Thesaurus thesaurus) {
        this.deviceService = deviceService;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
        return this;
    }

    @Override
    public void process(Message message) {
        LoadProfileOnDeviceQueueMessage qMessage;
        try {
            qMessage = jsonService.deserialize(message.getPayload(), LoadProfileOnDeviceQueueMessage.class);
        } catch (JsonDeserializeException e) {
            LOGGER.log(Level.WARNING, "Could not deserialize message - ignoring: " + message.getPayload());
            return;
        }

        Optional<Device> device = deviceService.findDeviceById(qMessage.deviceId);
        if (!device.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.NO_SUCH_DEVICE).format(qMessage.deviceId));
            return;
        }

        Optional<LoadProfile> loadProfile = device.get().getLoadProfiles()
                .stream()
                .filter(lp -> lp.getLoadProfileSpec().getLoadProfileType().getName().equals(qMessage.loadProfileName))
                .findFirst();

        if (!loadProfile.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.NO_SUCH_LOAD_PROFILE_ON_DEVICE).format(qMessage.deviceId, qMessage.loadProfileName));
            return;
        }

        Instant newStart = new Date(qMessage.lastReading).toInstant();
        Optional<Instant> lastReading = Optional.ofNullable(loadProfile.get().getLastReading()).map(Date::toInstant);
        if (!lastReading.isPresent() || lastReading.get().compareTo(newStart) != 0) {
            loadProfile.get().getDevice().getLoadProfileUpdaterFor(loadProfile.get()).setLastReading(newStart).update();
        }
    }

    @Override
    public void onMessageDelete(Message message) {
        //do nothing
    }
}
