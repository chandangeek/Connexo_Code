/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DevicesForConfigChangeSearch;
import com.energyict.mdc.device.data.ItemizeConfigChangeQueueMessage;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.exceptions.InvalidSearchDomain;
import com.energyict.mdc.device.data.exceptions.NoDestinationSpecFound;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import org.osgi.service.event.EventConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
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
    private static final Logger LOGGER = Logger.getLogger(DeviceConfigChangeHandler.class.getName());
    public static final String deviceConfigurationSearchPropertyName = "deviceConfiguration";
    public static final String deviceTypeSearchPropertyName = "deviceType";

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
                DeviceConfigChangeRequest deviceConfigChangeRequest = getDeviceConfigChangeRequest(configChangeContext, queueMessage.deviceConfigChangeRequestId);
                getDeviceStream(configChangeContext, queueMessage).forEach(consumeAndFilterDevices(configChangeContext,
                        device -> {
                            DeviceConfigChangeInActionImpl deviceConfigChangeInAction = deviceConfigChangeRequest.addDeviceInAction(device);
                            sendMessageOnConfigQueue(configChangeContext,
                                    configChangeContext.jsonService.serialize(
                                            new SingleConfigChangeQueueMessage(device.getId(), queueMessage.destinationDeviceConfigurationId, deviceConfigChangeInAction.getId(), queueMessage.deviceConfigChangeRequestId)),
                                    ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_SINGLE_START_ACTION);
                        }));
            }

            private Stream<Device> getDeviceStream(ConfigChangeContext configChangeContext, ItemizeConfigChangeQueueMessage queueMessage) {
                if (queueMessage.deviceIds.isEmpty() && queueMessage.search != null) {

                    /* ***********************************************************************************************
                     * We currently only support this if the user selected the DeviceType and a single DeviceConfig  *
                     * ***********************************************************************************************/
                    validateUniqueDeviceConfiguration(queueMessage.search, configChangeContext.thesaurus);

                    SearchDomain searchDomain = configChangeContext.searchService.findDomain(Device.class.getName())
                            .orElseThrow(() -> new InvalidSearchDomain(configChangeContext.thesaurus, Device.class.getName()));
                    SearchBuilder<Object> searchBuilder = configChangeContext.searchService.search(searchDomain);
                    for (SearchablePropertyValue propertyValue : searchDomain.getPropertiesValues(getPropertyMapper(queueMessage))) {
                        try {
                            propertyValue.addAsCondition(searchBuilder);
                        } catch (InvalidValueException e) {
                            throw DeviceConfigurationChangeException.invalidSearchValueForBulkConfigChange(configChangeContext.thesaurus, propertyValue.getProperty().getName());
                        }
                    }
                    return searchBuilder.toFinder().stream().map(Device.class::cast);
                } else {
                    return queueMessage.deviceIds.stream().map(configChangeContext.deviceService::findDeviceById).filter(Optional::isPresent).map(Optional::get);
                }
            }

            private Function<SearchableProperty, SearchablePropertyValue> getPropertyMapper(ItemizeConfigChangeQueueMessage queueMessage) {
                return searchableProperty -> new SearchablePropertyValue(searchableProperty, queueMessage.search.searchItems.get(searchableProperty.getName()));
            }

            private void validateUniqueDeviceConfiguration(DevicesForConfigChangeSearch search, Thesaurus thesaurus) {
                SearchablePropertyValue.ValueBean deviceConfigValueBean = search.searchItems.get(deviceConfigurationSearchPropertyName);
                if (deviceConfigValueBean == null
                        || deviceConfigValueBean.values == null
                        || deviceConfigValueBean.values.isEmpty()) {
                    throw DeviceConfigurationChangeException.needToSearchOnDeviceConfigForBulkAction(thesaurus);
                } else if (deviceConfigValueBean.values.size() > 1) {
                    throw DeviceConfigurationChangeException.needToSearchOnSingleDeviceConfigForBulkAction(thesaurus);
                }
            }

            private Consumer<Device> consumeAndFilterDevices(ConfigChangeContext configChangeContext, Consumer<Device> consumerForAllowedDevices) {
                return device -> {
                    if (!DefaultState.DECOMMISSIONED.getKey().equals(device.getState().getName())){
                        consumerForAllowedDevices.accept(device);
                    } else {
                        LOGGER.warning(configChangeContext.thesaurus.getFormat(MessageSeeds.CHANGE_CONFIG_WRONG_DEVICE_STATE)
                                .format(device.getName(), getStateName(configChangeContext.deviceLifeCycleConfigurationService, device.getState())));
                    }
                };
            }

            String getStateName(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, State state) {
                return DefaultState
                        .from(state)
                        .map(deviceLifeCycleConfigurationService::getDisplayName)
                        .orElseGet(state::getName);
            }
        },
        CONFIGCHANGEEXECUTOR(ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_SINGLE_START_ACTION) {
            @Override
            void handle(Map<String, Object> properties, ConfigChangeContext configChangeContext) {
                SingleConfigChangeQueueMessage queueMessage = configChangeContext.jsonService.deserialize(((String) properties.get(ServerDeviceForConfigChange.CONFIG_CHANGE_MESSAGE_VALUE)), SingleConfigChangeQueueMessage.class);
                Device device = configChangeContext.deviceService.findDeviceById(queueMessage.deviceId)
                        .orElseThrow(DeviceConfigurationChangeException.noDeviceFoundForConfigChange(configChangeContext.thesaurus, queueMessage.deviceId));
                Device deviceWithNewConfig = new DeviceConfigChangeExecutor(configChangeContext.deviceService, configChangeContext.deviceDataModelService.clock()).execute((DeviceImpl) device, configChangeContext.deviceDataModelService.deviceConfigurationService().findDeviceConfiguration(queueMessage.destinationDeviceConfigurationId).get());
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
        final DeviceConfigurationService deviceConfigurationService;
        final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

        public ConfigChangeContext(MessageService messageService, JsonService jsonService, SearchService searchService, Thesaurus thesaurus, ServerDeviceService deviceService, DeviceDataModelService deviceDataModelService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
            this.messageService = messageService;
            this.jsonService = jsonService;
            this.searchService = searchService;
            this.thesaurus = thesaurus;
            this.deviceService = deviceService;
            this.deviceDataModelService = deviceDataModelService;
            this.deviceConfigurationService = deviceConfigurationService;
            this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        }
    }
}
