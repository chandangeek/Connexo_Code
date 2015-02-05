package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.impl.events.ComTaskEnablementConnectionMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementPriorityMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementStatusMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.security.Privileges;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the Installer for the Device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer {

    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_NAME = "COMSCHED_RECALCULATOR";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME = "COMSCHED_BATCH_OBSOLETE";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final int KPI_CALCULATOR_TASK_RETRY_DELAY = 60;

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final UserService userService;
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    public Installer(DataModel dataModel, EventService eventService, MessageService messageService, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
        this.userService = userService;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        try {
            this.createPrivileges();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        this.createEventTypes();
        this.createMessageHandlers();
        this.addJupiterEventSubscribers();
        this.createMasterData();
        this.createKpiCalculatorDestination();
    }

    private void addJupiterEventSubscribers() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if(destinationSpec.isPresent()){
            DestinationSpec jupiterEvents = destinationSpec.get();
            Arrays.asList(
                    ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_NAME,
                    ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_NAME,
                    ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_NAME).stream().
                    filter(subscriber->!jupiterEvents.getSubscribers().stream().anyMatch(s->s.getName().equals(subscriber))).
                    forEach(jupiterEvents::subscribe);
        }
    }

    private void createKpiCalculatorDestination() {

        DestinationSpec destination =
                this.messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get().
                        createDestinationSpec(
                                DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION,
                                KPI_CALCULATOR_TASK_RETRY_DELAY);
        destination.activate();
        destination.subscribe(DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER);
    }

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges("MDC", "device.devices", "device.devices.description", new String[]{Privileges.ADD_DEVICE, Privileges.VIEW_DEVICE, Privileges.REMOVE_DEVICE});
        this.userService.createResourceWithPrivileges("MDC", "deviceData.deviceData", "deviceData.deviceData.description", new String[]{Privileges.ADMINISTRATE_DEVICE_DATA});
        this.userService.createResourceWithPrivileges("MDC", "deviceCommunication.deviceCommunications", "deviceCommunication.deviceCommunications.description", new String[]{Privileges.ADMINISTRATE_DEVICE_COMMUNICATION,Privileges.OPERATE_DEVICE_COMMUNICATION});
        this.userService.createResourceWithPrivileges("MDC", "deviceGroup.deviceGroups", "deviceGroup.deviceGroups.description", new String[]{Privileges.ADMINISTRATE_DEVICE_GROUP, Privileges.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.VIEW_DEVICE_GROUP_DETAIL});
        this.userService.createResourceWithPrivileges("MDC", "inventoryManagement.inventoryManagements", "inventoryManagement.inventoryManagements.description", new String[]{Privileges.IMPORT_INVENTORY_MANAGEMENT, Privileges.REVOKE_INVENTORY_MANAGEMENT});
    }

    private void createMessageHandlers() {
        try {
            this.createMessageHandler(COMSCHEDULE_RECALCULATOR_MESSAGING_NAME);
            this.createMessageHandler(COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void createMessageHandler(String messagingName) {
        try {
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(messagingName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            destinationSpec.activate();
            destinationSpec.subscribe(messagingName);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void createMasterData() {
        // No master data so far
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.createIfNotExists(this.eventService);
            } catch (Exception e) {
                this.logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}