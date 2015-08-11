package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.impl.ComScheduleOnDeviceQueueMessage;
import com.energyict.mdc.scheduling.SchedulingService;
import com.google.common.base.Strings;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * This message handler will add/remove comschedules on devices, either listed exhaustively or defined by a filter.
 */
public class ComScheduleOnDeviceFilterItimizer implements MessageHandler {

    private DeviceService deviceService;
    private MessageService messageService;
    private JsonService jsonService;

    @Override
    public void process(Message message) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            ItemizeComScheduleQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), ItemizeComScheduleQueueMessage.class);
            for (Long scheduleId : queueMessage.scheduleIds) {
                Stream<Device> deviceStream;
                if (queueMessage.filter !=null) {
                    Condition deviceSearchCondition = buildFilterFromJsonQuery(queueMessage.filter);
                    deviceStream = deviceService.findAllDevices(deviceSearchCondition).stream();
                }
                else {
                    deviceStream = queueMessage.deviceMRIDs.stream().map(deviceService::findByUniqueMrid).filter(Optional::isPresent).map(Optional::get);
                }
                deviceStream.forEach(
                        device -> processMessagePost(new ComScheduleOnDeviceQueueMessage(scheduleId, device.getmRID(), queueMessage.action), destinationSpec.get()));
            }
        } else {
            // LOG failure
        }
    }

    private void processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
    }

    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(MessageService messageService, JsonService jsonService, DeviceService deviceService) {
        this.messageService = messageService;
        this.jsonService = jsonService;
        this.deviceService = deviceService;
        return this;
    }

    private Condition buildFilterFromJsonQuery(ComScheduleOnDevicesFilterSpecification primitiveFilter) {
        Condition condition = Condition.TRUE;
        if (primitiveFilter.deviceTypes!=null) {
            condition=condition.and(primitiveFilter.deviceTypes.stream().map(id->Where.where("deviceConfiguration.deviceType.id").isEqualTo(id)).collect(toCondition()));
        }
        if (primitiveFilter.deviceConfigurations!=null) {
            condition=condition.and(primitiveFilter.deviceConfigurations.stream().map(id->Where.where("deviceConfiguration.id").isEqualTo(id)).collect(toCondition()));
        }
        if (!Strings.isNullOrEmpty(primitiveFilter.mRID)) {
            condition=condition.and(where("mRID").likeIgnoreCase(primitiveFilter.mRID));
        }
        if (!Strings.isNullOrEmpty(primitiveFilter.serialNumber)) {
            condition=condition.and(where("serialNumber").likeIgnoreCase(primitiveFilter.mRID));
        }
        return condition;
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
