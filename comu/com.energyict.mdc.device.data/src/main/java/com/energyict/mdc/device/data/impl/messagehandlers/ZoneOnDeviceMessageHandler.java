package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.ZoneOnDeviceQueueMessage;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZoneOnDeviceMessageHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(ZoneOnDeviceMessageHandler.class.getSimpleName());

    private TransactionService transactionService;
    private MeteringService meteringService;
    private DeviceService deviceService;
    private MeteringZoneService meteringZoneService;
    private JsonService jsonService;
    private Thesaurus thesaurus;

    public MessageHandler init(MeteringService meteringService, DeviceService deviceService, MeteringZoneService meteringZoneService, JsonService jsonService, TransactionService transactionService, Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.meteringZoneService = meteringZoneService;
        this.jsonService = jsonService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
        return this;
    }

    @Override
    public void process(Message message) {
        ZoneOnDeviceQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), ZoneOnDeviceQueueMessage.class);
        Optional<Zone> zone = meteringZoneService.getZone(queueMessage.zoneId);
        if (!zone.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.NO_SUCH_ZONE).format(queueMessage.zoneId));
            return;
        }
        Optional<Device> device = deviceService.findDeviceById(queueMessage.deviceId);
        if (!device.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.NO_SUCH_DEVICE).format(queueMessage.deviceId));
            return;
        }

        switch (queueMessage.action) {
            case Add:
                addZone(zone.get(), device.get(), queueMessage);
                break;
            case Remove:
                removeZone(zone.get(), device.get(), queueMessage);
                break;
            default:
                LOGGER.log(Level.WARNING, "Unknown action for Zone on device: " + queueMessage.action);
        }
    }

    @Override
    public void onMessageDelete(Message message) {
        //do nothing
    }

    private void addZone(Zone zone, Device device, ZoneOnDeviceQueueMessage queueMessage) {

        Optional<EndDeviceZone> endDeviceZone = endDeviceZoneByZoneType(zone, device);
        try {
            if (!endDeviceZone.isPresent()) {
                meteringZoneService.newEndDeviceZoneBuilder()
                        .withEndDevice(meteringService
                                .findEndDeviceByName(device.getName()).get())
                        .withZone(zone)
                        .create();

            } else {
                endDeviceZone.get().setZone(zone);
                endDeviceZone.get().save();
            }
        } catch(Exception e){
            LOGGER.log(Level.WARNING, e.getLocalizedMessage());
        }
    }


    private void removeZone(Zone zone, Device device, ZoneOnDeviceQueueMessage queueMessage) {
        try {
            meteringZoneService.getByEndDevice(meteringService.findEndDeviceByName(device.getName()).get())
                    .stream()
                    .filter(endDeviceZone->endDeviceZone.getZone().getId() == zone.getId())
                    .findFirst().ifPresent(endDeviceZone -> endDeviceZone.delete());
            LOGGER.info(thesaurus.getFormat(MessageSeeds.ZONE_REMOVED).format(queueMessage.zoneId, device.getName()));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    public Optional<EndDeviceZone> endDeviceZoneByZoneType(Zone zone, Device device){
        Optional<EndDevice> endDevice = meteringService.findEndDeviceByName(device.getName());
        if(endDevice.isPresent()) {
            return meteringZoneService.getByEndDevice(endDevice.get())
                    .stream()
                    .filter(endDeviceZone -> endDeviceZone.getZone().getZoneType().getId() == zone.getZoneType().getId())
                    .findFirst();
        }

        return Optional.empty();
    }
}
