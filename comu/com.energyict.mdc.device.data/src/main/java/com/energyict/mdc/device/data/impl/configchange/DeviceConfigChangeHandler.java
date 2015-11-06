package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ItemizeConfigChangeQueueMessage;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.exceptions.InvalidSearchDomain;
import com.energyict.mdc.device.data.exceptions.NoDestinationSpecFound;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import org.osgi.service.event.EventConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This message handler that processes all messages related to config changes:
 * <ul>
 * <li>Delegating to single actions</li>
 * <li>Execution config changes on a single device</li>
 * <li>Checking to release the Business Lock</li>
 * </ul>
 */
public class DeviceConfigChangeHandler implements MessageHandler {

    private final JsonService jsonService;
    private final ConfigChangeContext configChangeContext;

    public DeviceConfigChangeHandler(JsonService jsonService, ConfigChangeContext configChangeContext) {
        this.jsonService = jsonService;
        this.configChangeContext = configChangeContext;
    }

    @Override
    public void process(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        String topic = (String) messageProperties.get(EventConstants.EVENT_TOPIC);
        Optional<ConfigChangeHandler> handler = ConfigChangeHandler.getHandlerForTopic(topic);
        if (handler.isPresent()) {
            handler.get().handle(messageProperties, this.configChangeContext);
        }
    }

    private enum ConfigChangeHandler {
        ITEMIZER(ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_BULK_SETUP_ACTION) {
            @Override
            void handle(Map<String, Object> properties, ConfigChangeContext configChangeContext) {
                ItemizeConfigChangeQueueMessage queueMessage = configChangeContext.jsonService.deserialize(((String) properties.get(ServerDeviceForConfigChange.CONFIG_CHANGE_MESSAGE_VALUE)), ItemizeConfigChangeQueueMessage.class);
                Stream<Device> deviceStream;
                if (queueMessage.deviceMRIDs.isEmpty() && queueMessage.search != null) {
                    SearchDomain searchDomain = configChangeContext.searchService.findDomain(Device.class.getName()).orElseThrow(() -> new InvalidSearchDomain(configChangeContext.thesaurus, Device.class.getName()));
                    SearchBuilder<Object> searchBuilder = configChangeContext.searchService.search(searchDomain);

                    //TODO complete the search
//                    searchDomain.getProperties().stream().
//                            filter(p -> queueMessage.search.searchItems.getPropertyValue(p) != null).
//                            forEach(searchableProperty -> {
//                                try {
//                                    if (searchableProperty.getSelectionMode() == SearchableProperty.SelectionMode.MULTI) {
//                                        searchBuilder.where(searchableProperty).in(getQueryParameterAsObjectList(queueMessage.filter, searchableProperty));
//                                    } else if (searchableProperty.getSpecification().getValueFactory().getValueType().equals(String.class)) {
//                                        searchBuilder.where(searchableProperty).likeIgnoreCase((String) getQueryParameterAsObject(queueMessage.filter, searchableProperty));
//                                    } else {
//                                        searchBuilder.where(searchableProperty).isEqualTo(getQueryParameterAsObject(queueMessage.filter, searchableProperty));
//                                    }
//                                } catch (InvalidValueException e) {
//                                    // LOG failure
//                                }
//                            });
                    deviceStream = searchBuilder.toFinder().stream().map(Device.class::cast);
                } else {
                    deviceStream = queueMessage.deviceMRIDs.stream().map(configChangeContext.deviceService::findByUniqueMrid).filter(Optional::isPresent).map(Optional::get);
                }
                DeviceConfigChangeRequest deviceConfigChangeRequest = getDeviceConfigChangeRequest(configChangeContext, queueMessage.deviceConfigChangeRequestId);
                deviceStream.forEach(
                        device -> {
                            DeviceConfigChangeInActionImpl deviceConfigChangeInAction = deviceConfigChangeRequest.addDeviceInAction(device);
                            sendMessageOnConfigQueue(configChangeContext,
                                    configChangeContext.jsonService.serialize(
                                            new SingleConfigChangeQueueMessage(device.getmRID(), queueMessage.destinationDeviceConfigurationId, deviceConfigChangeInAction.getId(), queueMessage.deviceConfigChangeRequestId)),
                                    ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_SINGLE_START_ACTION);
                        });
            }
        },
        CONFIGCHANGEEXECUTOR(ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_SINGLE_START_ACTION) {
            @Override
            void handle(Map<String, Object> properties, ConfigChangeContext configChangeContext) {
                SingleConfigChangeQueueMessage queueMessage = configChangeContext.jsonService.deserialize(((String) properties.get(ServerDeviceForConfigChange.CONFIG_CHANGE_MESSAGE_VALUE)), SingleConfigChangeQueueMessage.class);
                Device device = configChangeContext.deviceService.findByUniqueMrid(queueMessage.deviceMrid).orElseThrow(DeviceConfigurationChangeException.noDeviceFoundForConfigChange(configChangeContext.thesaurus, queueMessage.deviceMrid));
                ((ServerDeviceForConfigChange) device).lock();
                Device deviceWithNewConfig = DeviceConfigChangeExecutor.getInstance().execute((DeviceImpl) device, configChangeContext.deviceDataModelService.deviceConfigurationService().findDeviceConfiguration(queueMessage.destinationDeviceConfigurationId).get());
                DeviceConfigChangeInAction deviceConfigInAction = getDeviceConfigInAction(configChangeContext, queueMessage.deviceConfigChangeInActionId);
                deviceConfigInAction.remove();
                sendMessageOnConfigQueue(configChangeContext, String.valueOf(queueMessage.deviceConfigChangeRequestId), ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_SINGLE_COMPLETED_ACTION);
            }
        },
        CLEANUP(ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_SINGLE_COMPLETED_ACTION) {
            @Override
            void handle(Map<String, Object> properties, ConfigChangeContext configChangeContext) {
                long deviceConfigChangeRequestId = Long.valueOf((String) properties.get(ServerDeviceForConfigChange.CONFIG_CHANGE_MESSAGE_VALUE));
                Optional<DeviceConfigChangeRequest> deviceConfigChangeRequest = configChangeContext.deviceService.findDeviceConfigChangeRequestById(deviceConfigChangeRequestId);
                // if it is not present, then it is probably already removed
                deviceConfigChangeRequest.ifPresent(DeviceConfigChangeRequest::notifyDeviceInActionIsRemoved);
            }
        };

        private final String topic;

        ConfigChangeHandler(String topic) {
            this.topic = topic;
        }

        String getTopic() {
            return this.topic;
        }

        abstract void handle(Map<String, Object> properties, ConfigChangeContext configChangeContext);

        Map<String, Object> createConfigChangeQueueMessage(String action, String messageValue) {
            Map<String, Object> message = new HashMap<>(2);
            message.put(EventConstants.EVENT_TOPIC, action);
            message.put(ServerDeviceForConfigChange.CONFIG_CHANGE_MESSAGE_VALUE, messageValue);
            return message;
        }

        DeviceConfigChangeRequest getDeviceConfigChangeRequest(ConfigChangeContext configChangeContext, long deviceConfigChangeRequestId) {
            return configChangeContext.deviceService.findDeviceConfigChangeRequestById(deviceConfigChangeRequestId)
                    .orElseThrow(DeviceConfigurationChangeException.noDeviceConfigChangeRequestFound(configChangeContext.thesaurus, deviceConfigChangeRequestId));
        }

        DeviceConfigChangeInAction getDeviceConfigInAction(ConfigChangeContext configChangeContext, long deviceConfigChangeInActionId) {
            return configChangeContext.deviceService.findDeviceConfigChangeInActionById(deviceConfigChangeInActionId)
                    .orElseThrow(DeviceConfigurationChangeException.noDeviceConfigChangeInActionFound(configChangeContext.thesaurus, deviceConfigChangeInActionId));
        }

        void sendMessageOnConfigQueue(ConfigChangeContext configChangeContext, String messageValue, String action) {
            configChangeContext.deviceDataModelService.messageService()
                    .getDestinationSpec(ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION)
                    .orElseThrow(new NoDestinationSpecFound(configChangeContext.thesaurus, ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION))
                    .message(
                            configChangeContext.jsonService.serialize(
                                    createConfigChangeQueueMessage(action, messageValue)))
                    .send();
        }

        public static Optional<ConfigChangeHandler> getHandlerForTopic(String topic) {
            return Stream.of(values()).filter(configChangeHandler -> configChangeHandler.getTopic().equals(topic)).findAny();
        }
    }

    public static class ConfigChangeContext {
        final MessageService messageService;
        final JsonService jsonService;
        final SearchService searchService;
        final Thesaurus thesaurus;
        final ServerDeviceService deviceService;
        final DeviceDataModelService deviceDataModelService;

        public ConfigChangeContext(MessageService messageService, JsonService jsonService, SearchService searchService, Thesaurus thesaurus, ServerDeviceService deviceService, DeviceDataModelService deviceDataModelService) {
            this.messageService = messageService;
            this.jsonService = jsonService;
            this.searchService = searchService;
            this.thesaurus = thesaurus;
            this.deviceService = deviceService;
            this.deviceDataModelService = deviceDataModelService;
        }
    }
}
