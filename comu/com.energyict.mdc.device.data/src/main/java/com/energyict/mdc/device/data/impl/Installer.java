package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementConnectionMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementPriorityMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementStatusMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.scheduling.SchedulingService;

import java.util.Arrays;
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

    private final static Logger LOGGER = Logger.getLogger(Installer.class.getName());

    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_NAME = "COMSCHED_RECALCULATOR";
    public static final String COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME = "Recalculate communication schedules";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME = "COMSCHED_BATCH_OBSOLETE";
    public static final String COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME = "Handle obsolete communication schedules";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;

    public Installer(DataModel dataModel, EventService eventService, MessageService messageService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        this.createEventTypes();
        this.createMessageHandlers();
        this.addJupiterEventSubscribers();
        this.createMasterData();
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
            this.createMessageHandler(defaultQueueTableSpec, DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION, DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER);
            this.createMessageHandler(defaultQueueTableSpec, ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION, ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_SUBSCRIBER);
            this.createMessageHandler(defaultQueueTableSpec, SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION, SchedulingService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER);
            this.createMessageHandler(defaultQueueTableSpec, SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION, SchedulingService.COM_SCHEDULER_QUEUE_SUBSCRIBER);
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