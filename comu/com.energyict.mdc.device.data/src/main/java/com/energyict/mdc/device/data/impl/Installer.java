package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.impl.ami.eventhandler.MeterReadingEventHandlerFactory;
import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementChangeMessageHandler;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementConnectionMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementPriorityMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementStatusMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.scheduling.SchedulingService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

/**
 * Represents the Installer for the Device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer implements FullInstaller {

    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_NAME = "COMSCHED_RECALCULATOR";
    static final String COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME = "Recalculate communication schedules";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME = "COMSCHED_BATCH_OBSOLETE";
    static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME = "Handle obsolete communication schedules";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final UserService userService;
    private final EventService eventService;
    private final MessageService messageService;
    private final InstallerV10_2Impl installerV10_2;

    @Inject
    public Installer(DataModel dataModel, UserService userService, EventService eventService, MessageService messageService, InstallerV10_2Impl installerV10_2) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
        this.eventService = eventService;
        this.messageService = messageService;
        this.installerV10_2 = installerV10_2;
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
        installerV10_2.install(dataModelUpgrader, logger);
        userService.addModulePrivileges(installerV10_2);
    }

    private void addJupiterEventSubscribers() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            DestinationSpec jupiterEvents = destinationSpec.get();
            Stream.of(
                    Pair.of(ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_NAME, whereCorrelationId().like("com/energyict/mdc/device/config/comtaskenablement/%")),
                    Pair.of(ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_NAME, whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/comtaskenablement/PRIORITY_UPDATED")),
                    Pair.of(ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_NAME, whereCorrelationId().like("com/energyict/mdc/device/config/comtaskenablement/%")),
                    Pair.of(MeterReadingEventHandlerFactory.SUBSCRIBER_NAME, whereCorrelationId().isEqualTo("com/elster/jupiter/metering/meterreading/CREATED")
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/connectiontask/COMPLETION"))))
                    .filter(subscriber -> !jupiterEvents.getSubscribers()
                            .stream()
                            .anyMatch(s -> s.getName().equals(subscriber.getFirst())))
                    .forEach(subscriber -> this.doSubscriber(jupiterEvents, subscriber));
        }
    }

    private void doSubscriber(DestinationSpec jupiterEvents, Pair<String, Condition> subscriber) {
        jupiterEvents.subscribe(subscriber.getFirst(), subscriber.getLast());
    }

    private void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandler(defaultQueueTableSpec, COMSCHEDULE_RECALCULATOR_MESSAGING_NAME);
        this.createMessageHandler(defaultQueueTableSpec, COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION, ConnectionTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_DESTINATION, ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DESTINATION, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION, CommunicationTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION, DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION, ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION, SchedulingService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION, SchedulingService.COM_SCHEDULER_QUEUE_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION, ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_SUBSCRIBER);
        this.createMessageHandler(defaultQueueTableSpec, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_DESTINATION, ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_SUBSCRIBER);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String messagingName) {
        createMessageHandler(defaultQueueTableSpec, messagingName, messagingName);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, String subscriberName) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberName);
        } else {
            boolean notSubscribedYet = !destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .anyMatch(spec -> spec.getName().equals(subscriberName));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberName);
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
}