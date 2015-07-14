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
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
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

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

/**
 * Represents the Installer for the Device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer {

    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_NAME = "COMSCHED_RECALCULATOR";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME = "COMSCHED_BATCH_OBSOLETE";
    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME = "Recalculate communication schedules";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME = "Handle obsolete communication schedules";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final int KPI_CALCULATOR_TASK_RETRY_DELAY = 60;

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final UserService userService;
    private final Logger logger = Logger.getLogger(Installer.class.getName());
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

    private void addTranslations() {
        addTranslation(DeviceDataServices.COMPONENT_NAME, DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER, DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, COMSCHEDULE_RECALCULATOR_MESSAGING_NAME, COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME, COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME);

        addTranslation(DeviceDataServices.COMPONENT_NAME, ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME);
        addTranslation(DeviceDataServices.COMPONENT_NAME, ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME);
    }

    private void addJupiterEventSubscribers() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            DestinationSpec jupiterEvents = destinationSpec.get();
            Arrays.asList(
                    Pair.of(ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_NAME, whereCorrelationId().like("com/energyict/mdc/device/config/comtaskenablement/%")),
                    Pair.of(ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_NAME, whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/comtaskenablement/PRIORITY_UPDATED")),
                    Pair.of(ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_NAME, whereCorrelationId().like("com/energyict/mdc/device/config/comtaskenablement/%"))).stream().
                    filter(subscriber -> !jupiterEvents.getSubscribers().stream().anyMatch(s -> s.getName().equals(subscriber.getFirst()))).
                    forEach(subscriber -> this.doSubscriber(jupiterEvents, subscriber));
        }
    }

    private void doSubscriber(DestinationSpec jupiterEvents, Pair<String, Condition> subscriber) {
        jupiterEvents.subscribe(subscriber.getFirst(), subscriber.getLast());
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
        this.userService.createResourceWithPrivileges("MDC", "deviceCommunication.deviceCommunications", "deviceCommunication.deviceCommunications.description", new String[]{Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION});
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