package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementConnectionMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementPriorityMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementStatusMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

    private final static Logger LOGGER = Logger.getLogger(Installer.class.getName());

    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_NAME = "COMSCHED_RECALCULATOR";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME = "COMSCHED_BATCH_OBSOLETE";
    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME = "Recalculate communication schedules";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME = "Handle obsolete communication schedules";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final UserService userService;
    private final Thesaurus thesaurus;

    public Installer(DataModel dataModel, EventService eventService, MessageService messageService, UserService userService, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
        this.userService = userService;
        this.thesaurus = thesaurus;
    }

    public void install(boolean executeDdl) {
        addTranslations();
        try {
            this.dataModel.install(executeDdl, true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        try {
            this.createPrivileges();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
        this.createEventTypes();
        this.createMessageHandlers();
        this.addJupiterEventSubscribers();
        this.createMasterData();
        this.createKpiCalculatorDestination();
    }

    private void addTranslations() {
        addTranslation(DeviceDataServices.COMPONENT_NAME, DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER, DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, COMSCHEDULE_RECALCULATOR_MESSAGING_NAME, COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME, COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME);

        addTranslation(DeviceDataServices.COMPONENT_NAME, ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_SUBSCRIBER, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, CommunicationTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER, CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_SUBSCRIBER, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, ConnectionTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER, ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_SUBSCRIBER, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_SUBSCRIBER, ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_DISPLAYNAME);
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
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        createMessageHandler(defaultQueueTableSpec, DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION, DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER);
    }

    private void addTranslation(String componentName, String subscriberName, String subscriberDisplayName) {
        NlsKey statusKey = SimpleNlsKey.key(componentName, Layer.DOMAIN, subscriberName);
        Translation statusTranslation = SimpleTranslation.translation(statusKey, Locale.ENGLISH, subscriberDisplayName);
        List<Translation> translations = new ArrayList<>();
        translations.add(statusTranslation);
        thesaurus.addTranslations(translations);
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
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            this.createMessageHandler(defaultQueueTableSpec, COMSCHEDULE_RECALCULATOR_MESSAGING_NAME);
            this.createMessageHandler(defaultQueueTableSpec, COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME);
            this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION, ConnectionTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER);
            this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_SUBSCRIBER);
            this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_DESTINATION, ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_SUBSCRIBER);
            this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DESTINATION, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_SUBSCRIBER);
            this.createMessageHandler(defaultQueueTableSpec, CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION, CommunicationTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER);
            this.createMessageHandler(defaultQueueTableSpec, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_SUBSCRIBER);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String messagingName) {
        createMessageHandler(defaultQueueTableSpec, messagingName, messagingName);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, String subscriberName) {
        try {
            Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
            if (!destinationSpecOptional.isPresent()) {
                DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
                queue.activate();
                queue.subscribe(subscriberName);
            } else {
                boolean alreadySubscribed = destinationSpecOptional.get().getSubscribers().stream().anyMatch(spec -> spec.getName().equals(subscriberName));
                if (!alreadySubscribed) {
                    destinationSpecOptional.get().activate();
                    destinationSpecOptional.get().subscribe(subscriberName);
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
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
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}