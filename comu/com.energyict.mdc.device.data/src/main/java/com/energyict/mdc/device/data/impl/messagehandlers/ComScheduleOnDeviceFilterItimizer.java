package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ItemizeComScheduleQueueMessage;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.impl.ComScheduleOnDeviceQueueMessage;
import com.energyict.mdc.scheduling.SchedulingService;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * This message handler will add/remove comschedules on devices, either listed exhaustively or defined by a filter.
 */
public class ComScheduleOnDeviceFilterItimizer implements MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(ComScheduleOnDeviceFilterItimizer.class.getName());

    private DeviceService deviceService;
    private MessageService messageService;
    private JsonService jsonService;
    private SearchService searchService;

    @Override
    public void process(Message message) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            ItemizeComScheduleQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), ItemizeComScheduleQueueMessage.class);
            for (Long scheduleId : queueMessage.scheduleIds) {
                Stream<Device> deviceStream;
                if (queueMessage.filter != null) {
                    Optional<SearchDomain> searchDomain = searchService.findDomain(Device.class.getName());
                    if (!searchDomain.isPresent()) {
                        break;
                    }
                    SearchBuilder<Object> searchBuilder = searchService.search(searchDomain.get());
                    for (SearchablePropertyValue propertyValue : searchDomain.get().getPropertiesValues(getPropertyMapper(queueMessage))) {
                        try {
                            propertyValue.addAsCondition(searchBuilder);
                        } catch (InvalidValueException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }
                    deviceStream = searchBuilder.toFinder().stream().map(Device.class::cast);
                } else {
                    deviceStream = queueMessage.deviceIds.stream().map(deviceService::findDeviceById).filter(Optional::isPresent).map(Optional::get);
                }
                deviceStream.forEach(
                        device -> processMessagePost(new ComScheduleOnDeviceQueueMessage(scheduleId, device.getId(), queueMessage.action, queueMessage.strategy), destinationSpec.get()));
            }
        } else {
            LOGGER.log(Level.SEVERE, "Destination '" + SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION + "' is not available");
        }
    }

    private Function<SearchableProperty, SearchablePropertyValue> getPropertyMapper(ItemizeComScheduleQueueMessage queueMessage) {
        return searchableProperty -> new SearchablePropertyValue(searchableProperty, queueMessage.filter.properties.get(searchableProperty.getName()));
    }

    private void processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
    }

    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(MessageService messageService, JsonService jsonService, DeviceService deviceService, SearchService searchService) {
        this.messageService = messageService;
        this.jsonService = jsonService;
        this.deviceService = deviceService;
        this.searchService = searchService;
        return this;
    }

    private Collector<Condition, Condition, Condition> toCondition() {
        return new Collector<Condition, Condition, Condition>() {

            @Override
            public BiConsumer<Condition, Condition> accumulator() {
                return Condition::or;
            }

            @Override
            public Supplier<Condition> supplier() {
                return () -> Condition.FALSE;
            }

            @Override
            public BinaryOperator<Condition> combiner() {
                return Condition::or;
            }

            @Override
            public Function<Condition, Condition> finisher() {
                return c -> (Condition) c;
            }

            @Override
            public Set<Collector.Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }
}
