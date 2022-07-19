/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonDeserializeException;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SetPushEventsToSapOnDeviceQueueMessage;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SetPushEventsToSapFlagOnDeviceMessageHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(SetPushEventsToSapFlagOnDeviceMessageHandler.class.getSimpleName());
    private DeviceService deviceService;
    private SAPCustomPropertySets sapCustomPropertySets;
    private JsonService jsonService;
    private Thesaurus thesaurus;

    public SetPushEventsToSapFlagOnDeviceMessageHandler(DeviceService deviceService, SAPCustomPropertySets sapCustomPropertySets,
                                                        JsonService jsonService, Thesaurus thesaurus) {
        this.deviceService = deviceService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void process(Message message) {
        SetPushEventsToSapOnDeviceQueueMessage qMessage;
        try {
            qMessage = jsonService.deserialize(message.getPayload(), SetPushEventsToSapOnDeviceQueueMessage.class);
        } catch (JsonDeserializeException e) {
            LOGGER.log(Level.SEVERE, "Could not deserialize message - ignoring: " + message.getPayload());
            return;
        }

        Optional<Device> device = deviceService.findDeviceById(qMessage.deviceId);
        if (!device.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.MessageSeeds.NO_SUCH_DEVICE).format(qMessage.deviceId));
            return;
        }

        sapCustomPropertySets.setPushEventsToSapFlag(device.get(), qMessage.pushEventsToSap);
    }

    @Override
    public void onMessageDelete(Message message) {
        //do nothing
    }
}