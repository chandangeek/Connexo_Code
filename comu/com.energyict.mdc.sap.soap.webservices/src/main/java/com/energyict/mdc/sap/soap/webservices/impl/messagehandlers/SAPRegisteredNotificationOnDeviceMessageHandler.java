/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.json.JsonDeserializeException;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SAPRegisteredNotificationOnDeviceQueueMessage;
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import java.time.Clock;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SAPRegisteredNotificationOnDeviceMessageHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(SAPRegisteredNotificationOnDeviceMessageHandler.class.getSimpleName());
    private DeviceService deviceService;
    private EndPointConfigurationService endPointConfigurationService;
    private UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification;
    private SAPCustomPropertySets sapCustomPropertySets;
    private Clock clock;
    private JsonService jsonService;
    private Thesaurus thesaurus;

    public SAPRegisteredNotificationOnDeviceMessageHandler(DeviceService deviceService, EndPointConfigurationService endPointConfigurationService,
                               UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification, SAPCustomPropertySets sapCustomPropertySets,
                               Clock clock, JsonService jsonService, Thesaurus thesaurus) {
        this.deviceService = deviceService;
        this.endPointConfigurationService = endPointConfigurationService;
        this.utilitiesDeviceRegisteredNotification = utilitiesDeviceRegisteredNotification;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.clock = clock;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void process(Message message) {
        SAPRegisteredNotificationOnDeviceQueueMessage qMessage;
        try {
            qMessage = jsonService.deserialize(message.getPayload(), SAPRegisteredNotificationOnDeviceQueueMessage.class);
        } catch (JsonDeserializeException e) {
            LOGGER.log(Level.SEVERE, "Could not deserialize message - ignoring: " + message.getPayload());
            return;
        }

        Optional<Device> device = deviceService.findDeviceById(qMessage.deviceId);
        if (!device.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.MessageSeeds.NO_SUCH_DEVICE).format(qMessage.deviceId));
            return;
        }

        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService.getEndPointConfiguration(qMessage.endPointId)
                .filter(EndPointConfiguration::isActive)
                .filter(e -> e.getWebServiceName().equals(UtilitiesDeviceRegisteredNotification.NAME));
        if (!endPointConfiguration.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.NO_REGISTERED_NOTIFICATION_ENDPOINT).format(qMessage.endPointId));
            return;
        }

        Optional<String> sapDeviceId = sapCustomPropertySets.getSapDeviceId(device.get());
        if (!sapDeviceId.isPresent()) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.DEVICE_ID_ATTRIBUTE_IS_NOT_SET).format());
            return;
        }

        if (!sapCustomPropertySets.isAnyLrnPresent(qMessage.deviceId, clock.instant())) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.NO_LRN).format());
            return;
        }

        if (!utilitiesDeviceRegisteredNotification.call(sapDeviceId.get(), Collections.singleton(endPointConfiguration.get()))) {
            LOGGER.log(Level.SEVERE, thesaurus.getFormat(MessageSeeds.REQUEST_SENDING_HAS_FAILED).format());
            return;
        }
    }

    @Override
    public void onMessageDelete(Message message) {
        //do nothing
    }
}
