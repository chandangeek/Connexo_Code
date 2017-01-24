package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ItemizeComTaskEnablementQueueMessage;
import com.energyict.mdc.device.data.exceptions.NoDestinationSpecFound;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.configchange.SingleComTaskEnablementQueueMessage;

import org.osgi.service.event.EventConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Handles events that are being sent when a ComTaskEnablement
 * has been added to a DeviceConfiguration or when an existing ComTaskEnablement
 * was updated.
 *
 * @author sva
 * @since 25/01/2016 - 12:11
 */
public class ComTaskEnablementChangeMessageHandler implements MessageHandler {

    public static final String COMTASK_ENABLEMENT_QUEUE_DESTINATION = "ComTaskEnablementQD";
    public static final String COMTASK_ENABLEMENT_QUEUE_SUBSCRIBER = "ComTaskEnablementSubscriber";
    public static final String COMTASK_ENABLEMENT_QUEUE_SUBSCRIBER_DISPLAY_NAME = "Handle inbound setting on communication task configuration";

    public static String COMTASK_ENABLEMENT_ACTION = "comtaskenablement/ACTION";
    public static String COMTASK_ENABLEMENT_MESSAGE_VALUE = "ComTaskEnablementMessageValue";
    public static String COMTASK_ENABLEMENT_SINGLE_ACTION = "comtaskenablement/START";

    private final JsonService jsonService;
    private final ComTaskEnablementConfig configChangeContext;

    public ComTaskEnablementChangeMessageHandler(JsonService jsonService, ComTaskEnablementConfig comTaskEnablementConfig) {
        this.jsonService = jsonService;
        this.configChangeContext = comTaskEnablementConfig;
    }

    @Override
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        String topic = (String) messageProperties.get(EventConstants.EVENT_TOPIC);
        Optional<ComTaskEnablementHandler> handler = ComTaskEnablementHandler.getHandlerForTopic(topic);
        if (handler.isPresent()) {
            handler.get().handle(messageProperties, this.configChangeContext);
        }
    }

    private enum ComTaskEnablementHandler {
        ITEMIZER(COMTASK_ENABLEMENT_ACTION) {
            @Override
            void handle(Map<String, Object> properties, ComTaskEnablementConfig comTaskEnablementConfig) {
                ItemizeComTaskEnablementQueueMessage queueMessage = comTaskEnablementConfig.jsonService.deserialize(((String) properties.get(COMTASK_ENABLEMENT_MESSAGE_VALUE)), ItemizeComTaskEnablementQueueMessage.class);
                ComTaskEnablement comTaskEnablement = getComTaskEnablement(comTaskEnablementConfig, queueMessage.comTaskEnablementId);
                if (comTaskEnablement.isIgnoreNextExecutionSpecsForInbound()) {
                    getDeviceStream(comTaskEnablementConfig, comTaskEnablement).filter(device ->
                            device.getComTaskExecutions().stream().noneMatch(cte -> cte.getComTask().getId() == comTaskEnablement.getComTask().getId())).
                            forEach(device -> sendMessageOnSingleQueue(comTaskEnablementConfig,
                                    comTaskEnablementConfig.jsonService.serialize(
                                            new SingleComTaskEnablementQueueMessage(device.getId(), queueMessage.comTaskEnablementId)),
                                    COMTASK_ENABLEMENT_SINGLE_ACTION));
                }
            }
        },
        EXECUTOR(COMTASK_ENABLEMENT_SINGLE_ACTION) {
            @Override
            void handle(Map<String, Object> properties, ComTaskEnablementConfig comTaskEnablementConfig) {
                SingleComTaskEnablementQueueMessage queueMessage = comTaskEnablementConfig.jsonService.deserialize(((String) properties.get(COMTASK_ENABLEMENT_MESSAGE_VALUE)), SingleComTaskEnablementQueueMessage.class);
                createComTaskExecutionsForEnablement(
                        getComTaskEnablement(comTaskEnablementConfig, queueMessage.comTaskEnablementId),
                        comTaskEnablementConfig.deviceService.findDeviceById(queueMessage.deviceId)
                                .orElseThrow(NoSuchElementException.deviceWithIdNotFound(comTaskEnablementConfig.thesaurus, queueMessage.deviceId))
                );
            }

            private void createComTaskExecutionsForEnablement(ComTaskEnablement comTaskEnablement, Device device) {
                if (device.getComTaskExecutions().stream().noneMatch(cte -> cte.getComTask().getId() == comTaskEnablement.getComTask().getId())) {
                    device.newManuallyScheduledComTaskExecution(comTaskEnablement, null).add();
                }
            }
        },;

        private final String topic;

        ComTaskEnablementHandler(String topic) {
            this.topic = topic;
        }

        String getTopic() {
            return this.topic;
        }

        abstract void handle(Map<String, Object> properties, ComTaskEnablementConfig comTaskEnablementConfig);

        Map<String, Object> createConfigChangeQueueMessage(String action, String messageValue) {
            Map<String, Object> message = new HashMap<>(2);
            message.put(EventConstants.EVENT_TOPIC, action);
            message.put(COMTASK_ENABLEMENT_MESSAGE_VALUE, messageValue);
            return message;
        }

        ComTaskEnablement getComTaskEnablement(ComTaskEnablementConfig comTaskEnablementConfig, long comTaskEnablementId) {
            return comTaskEnablementConfig.deviceConfigurationService.findComTaskEnablement(comTaskEnablementId)
                    .orElseThrow(NoSuchElementException.comTaskEnablementWithIdNotFound(comTaskEnablementConfig.thesaurus, comTaskEnablementId));
        }

        Stream<Device> getDeviceStream(ComTaskEnablementConfig comTaskEnablementConfig, ComTaskEnablement comTaskEnablement) {
            return comTaskEnablementConfig.deviceService.findDevicesByDeviceConfiguration(comTaskEnablement.getDeviceConfiguration()).stream();
        }

        void sendMessageOnSingleQueue(ComTaskEnablementConfig comTaskEnablementConfig, String messageValue, String action) {
            comTaskEnablementConfig.deviceDataModelService.messageService()
                    .getDestinationSpec(COMTASK_ENABLEMENT_QUEUE_DESTINATION)
                    .orElseThrow(new NoDestinationSpecFound(comTaskEnablementConfig.thesaurus, COMTASK_ENABLEMENT_QUEUE_DESTINATION))
                    .message(
                            comTaskEnablementConfig.jsonService.serialize(
                                    createConfigChangeQueueMessage(action, messageValue)))
                    .send();
        }

        public static Optional<ComTaskEnablementHandler> getHandlerForTopic(String topic) {
            return Stream.of(values()).filter(comTaskEnablementHandler -> comTaskEnablementHandler.getTopic().equals(topic)).findAny();
        }
    }

    public static class ComTaskEnablementConfig {

        final MessageService messageService;
        final JsonService jsonService;
        final Thesaurus thesaurus;
        final ServerDeviceService deviceService;
        final DeviceDataModelService deviceDataModelService;
        final DeviceConfigurationService deviceConfigurationService;

        public ComTaskEnablementConfig(MessageService messageService, JsonService jsonService, Thesaurus thesaurus, ServerDeviceService deviceService, DeviceDataModelService deviceDataModelService, DeviceConfigurationService deviceConfigurationService) {
            this.messageService = messageService;
            this.jsonService = jsonService;
            this.thesaurus = thesaurus;
            this.deviceService = deviceService;
            this.deviceDataModelService = deviceDataModelService;
            this.deviceConfigurationService = deviceConfigurationService;
        }
    }
}