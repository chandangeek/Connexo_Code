package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.OnDemandReadServiceCallHandler;
import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementChangeMessageHandler;
import com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.scheduling.SchedulingService;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;
import static com.energyict.mdc.device.data.impl.SyncDeviceWithKoreMeter.MULTIPLIER_TYPE;

/**
 * Represents the Installer for the Device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer implements FullInstaller, PrivilegesProvider {

    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_NAME = "COMSCHED_RECALCULATOR";
    static final String COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME = "Recalculate communication schedules";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME = "COMSCHED_BATCH_OBSOLETE";
    static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME = "Handle obsolete communication schedules";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final UserService userService;
    private final EventService eventService;
    private final MessageService messageService;
    private final MeteringService meteringService;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public Installer(DataModel dataModel, UserService userService, EventService eventService, MessageService messageService, MeteringService meteringService, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
        this.eventService = eventService;
        this.messageService = messageService;
        this.meteringService = meteringService;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for ",
                this::createEventTypes,
                logger
        );
        doTry(
                "Create message handlers",
                this::createMessageHandlers,
                logger
        );
        doTry(
                "Create event subscribers",
                this::addJupiterEventSubscribers,
                logger
        );
        doTry(
                "Create master data",
                this::createMasterData,
                logger
        );
        doTry(
                "Create service call types",
                this::createServiceCallTypes,
                logger
        );
        doTry(
                "Create default multiplier type",
                this::createDefaultMultiplierType,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return DeviceDataServices.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {

        return Arrays.asList(
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICES.getKey(), Privileges.RESOURCE_DEVICES_DESCRIPTION.getKey(), Arrays.asList(Privileges.Constants.ADMINISTRATE_DEVICE, Privileges.Constants.VIEW_DEVICE, Privileges.Constants.REMOVE_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_ATTRIBUTE)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_DATA.getKey(), Privileges.RESOURCE_DEVICE_DATA_DESCRIPTION.getKey(), Arrays
                        .asList(Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_COMMUNICATIONS.getKey(), Privileges.RESOURCE_DEVICE_COMMUNICATIONS_DESCRIPTION
                        .getKey(), Arrays.asList(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_GROUPS.getKey(), Privileges.RESOURCE_DEVICE_GROUPS_DESCRIPTION.getKey(), Arrays
                        .asList(Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_INVENTORY_MANAGEMENT.getKey(), Privileges.RESOURCE_INVENTORY_MANAGEMENT_DESCRIPTION
                        .getKey(), Arrays.asList(Privileges.Constants.IMPORT_INVENTORY_MANAGEMENT, Privileges.Constants.REVOKE_INVENTORY_MANAGEMENT)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DATA_COLLECTION_KPI.getKey(), Privileges.RESOURCE_DATA_COLLECTION_KPI_DESCRIPTION
                        .getKey(), Arrays.asList(Privileges.Constants.ADMINISTER_DATA_COLLECTION_KPI, Privileges.Constants.VIEW_DATA_COLLECTION_KPI)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICES
                        .getKey(), Privileges.RESOURCE_DEVICES_DESCRIPTION.getKey(), Collections
                        .singletonList(Privileges.Constants.ADMINISTER_DEVICE_TIME_SLICED_CPS))
        );

    }

    private void addJupiterEventSubscribers() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            DestinationSpec jupiterEvents = destinationSpec.get();
            Stream.of(
                    Pair.of(SubscriberTranslationKeys.COMTASK_ENABLEMENT_CONNECTION, whereCorrelationId().like("com/energyict/mdc/device/config/comtaskenablement/%")),
                    Pair.of(SubscriberTranslationKeys.COMTASK_ENABLEMENT_PRIORITY, whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/comtaskenablement/PRIORITY_UPDATED")),
                    Pair.of(SubscriberTranslationKeys.COMTASK_ENABLEMENT_STATUS, whereCorrelationId().like("com/energyict/mdc/device/config/comtaskenablement/%")),
                    Pair.of(SubscriberTranslationKeys.METER_READING_EVENT, whereCorrelationId().isEqualTo("com/elster/jupiter/metering/meterreading/CREATED")
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/connectiontask/COMPLETION"))))
                    .filter(subscriber -> !jupiterEvents.getSubscribers()
                                    .stream()
                                    .anyMatch(s -> s.getName().equals(subscriber.getFirst())))
                    .forEach(subscriber -> this.doSubscriber(jupiterEvents, subscriber));
        }
    }

    private void doSubscriber(DestinationSpec jupiterEvents, Pair<SubscriberTranslationKeys, Condition> subscriber) {
        jupiterEvents.subscribe(subscriber.getFirst(), DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN, subscriber.getLast());
    }

    private void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandler(defaultQueueTableSpec, SubscriberTranslationKeys.COMSCHEDULE_RECALCULATOR);
        this.createMessageHandler(defaultQueueTableSpec, SubscriberTranslationKeys.COMSCHEDULE_BACKGROUND_OBSOLETION);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION, SubscriberTranslationKeys.CONNECTION_FILTER_ITEMIZER);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION, SubscriberTranslationKeys.CONNECTION_RESCHEDULER);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_DESTINATION, SubscriberTranslationKeys.CONNECTION_PROPERTY_FILTER_ITEMIZER);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DESTINATION, SubscriberTranslationKeys.CONNECTION_PROPERTY_UPDATER);
        this.createMessageHandler(defaultQueueTableSpec, CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION, SubscriberTranslationKeys.COMMUNICATION_FILTER_ITEMIZER);
        this.createMessageHandler(defaultQueueTableSpec, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION, SubscriberTranslationKeys.COMMUNICATION_RESCHEDULER);
        this.createMessageHandler(defaultQueueTableSpec, DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION, SubscriberTranslationKeys.DATA_COLLECTION_KPI_CALCULATOR);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION, SubscriberTranslationKeys.CONNECTION_TASK_VALIDATOR_AFTER_PROPERTY_REMOVAL);
        this.createMessageHandler(defaultQueueTableSpec, SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION, SubscriberTranslationKeys.COMSCHEDULE_FILTER_ITEMIZER);
        this.createMessageHandler(defaultQueueTableSpec, SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION, SubscriberTranslationKeys.COMSCHEDULE);
        this.createMessageHandler(defaultQueueTableSpec, ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION, SubscriberTranslationKeys.CHANGE_DEVICE_CONFIGURATION);
        this.createMessageHandler(defaultQueueTableSpec, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_DESTINATION, SubscriberTranslationKeys.COMTASK_ENABLEMENT);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, TranslationKey nameKey) {
        createMessageHandler(defaultQueueTableSpec, nameKey.getKey(), nameKey);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = !destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .anyMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
            }
        }
    }

    private void createMasterData() {
        // No master data so far
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.createIfNotExists(this.eventService);
        }
    }

    private void createServiceCallTypes() {
        for (ServiceCallCommands.ServiceCallTypeMapping serviceCallTypeMapping : ServiceCallCommands.ServiceCallTypeMapping.values()) {
            createServiceCallType(serviceCallTypeMapping);
        }
        createOnDemandReadServiceCallType();
    }

    private void createOnDemandReadServiceCallType() {
        RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet()
                .getId())
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find custom property set ''{0}''", OnDemandReadServiceCallCustomPropertySet.class
                        .getSimpleName())));
        RegisteredCustomPropertySet completionOptionsCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(new CompletionOptionsCustomPropertySet()
                .getId())
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find custom property set ''{0}''", CompletionOptionsCustomPropertySet.class
                        .getSimpleName())));


        serviceCallService.findServiceCallType(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME, OnDemandReadServiceCallHandler.VERSION)
                .orElseGet(() -> serviceCallService.createServiceCallType(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME, OnDemandReadServiceCallHandler.VERSION)
                        .handler(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                        .customPropertySet(customPropertySet)
                        .customPropertySet(completionOptionsCustomPropertySet)
                        .create());
    }

    private void createServiceCallType(ServiceCallCommands.ServiceCallTypeMapping serviceCallTypeMapping) {
        Optional<ServiceCallType> serviceCallTypeOptional = serviceCallService.findServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion());
        if (!serviceCallTypeOptional.isPresent()) {
            RegisteredCustomPropertySet commandCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(new CommandCustomPropertySet().getId())
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}", CommandCustomPropertySet.class.getSimpleName())));
            RegisteredCustomPropertySet completionOptionsCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(new CompletionOptionsCustomPropertySet().getId())
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}", CommandCustomPropertySet.class.getSimpleName())));

            serviceCallService.createServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion())
                    .handler(serviceCallTypeMapping.getTypeName())
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(commandCustomPropertySet)
                    .customPropertySet(completionOptionsCustomPropertySet)
                    .create();
        }
    }

    private void createDefaultMultiplierType() {
        this.meteringService.createMultiplierType(MULTIPLIER_TYPE);
    }

}