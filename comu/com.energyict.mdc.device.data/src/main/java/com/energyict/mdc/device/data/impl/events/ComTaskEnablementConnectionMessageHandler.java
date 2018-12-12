/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.tasks.ComTask;

import org.osgi.service.event.EventConstants;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Handles events that are being sent when connection related changes
 * have been applied to a {@link ComTaskEnablement}.<br>
 * The changes that are being monitored are listed and described
 * <a href="http://confluence.eict.vpdc/display/JUPMDC/ComTaskEnablement+events">here</a>.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (11:51)
 */
public class ComTaskEnablementConnectionMessageHandler implements MessageHandler {

    private final JsonService jsonService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ServerCommunicationTaskService communicationTaskService;

    public ComTaskEnablementConnectionMessageHandler(JsonService jsonService, DeviceConfigurationService deviceConfigurationService, ServerCommunicationTaskService communicationTaskService) {
        super();
        this.jsonService = jsonService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.communicationTaskService = communicationTaskService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        String topic = (String) messageProperties.get(EventConstants.EVENT_TOPIC);
        ActualEventHandler.forTopic(topic).process(messageProperties, new ServiceLocatorImpl());
    }

    private interface ServiceLocator {
        public DeviceConfigurationService deviceConfigurationService();

        public ServerCommunicationTaskService communicationTaskService();
    }

    private class ServiceLocatorImpl implements ComTaskEnablementConnectionMessageHandler.ServiceLocator {
        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return deviceConfigurationService;
        }

        @Override
        public ServerCommunicationTaskService communicationTaskService() {
            return communicationTaskService;
        }
    }

    private abstract static class ConnectionStrategyChangeEventData {
        private final DeviceConfigurationService deviceConfigurationService;
        private final Map<String, Object> messageProperties;

        protected ConnectionStrategyChangeEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super();
            this.deviceConfigurationService = deviceConfigurationService;
            this.messageProperties = messageProperties;
        }

        protected DeviceConfigurationService getDeviceConfigurationService() {
            return deviceConfigurationService;
        }

        protected ComTaskEnablement getComTaskEnablement() {
            return this.deviceConfigurationService.findComTaskEnablement(this.getLong("comTaskEnablementId")).get();
        }

        protected Long getLong(String key) {
            Object contents = this.messageProperties.get(key);
            if (contents instanceof Long) {
                return (Long) contents;
            } else {
                return ((Integer) contents).longValue();
            }
        }

        protected ConnectionFunction getConnectionFunction(DeviceConfiguration deviceConfiguration, long id) {
            Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass();
            if (deviceProtocolPluggableClass.isPresent()) {
                return deviceProtocolPluggableClass.get().getConsumableConnectionFunctions().stream()
                        .filter(connectionFunction -> connectionFunction.getId() == id)
                        .findFirst().orElse(null);
            }
            return null;
        }
    }

    private static class SwitchFromDefaultConnectionToPartialConnectionTaskEventData extends ConnectionStrategyChangeEventData {
        protected SwitchFromDefaultConnectionToPartialConnectionTaskEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private PartialConnectionTask getPartialConnectionTask() {
            return getDeviceConfigurationService().findPartialConnectionTask(this.getLong("partialConnectionTaskId")).get();
        }
    }

    private static class SwitchFromDefaultConnectionToConnectionFunctionEventData extends ConnectionStrategyChangeEventData {
        protected SwitchFromDefaultConnectionToConnectionFunctionEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private ConnectionFunction getConnectionFunction() {
            return getConnectionFunction(getComTaskEnablement().getDeviceConfiguration(), this.getLong("newConnectionFunctionId"));
        }
    }

    private static class SwitchFromPartialConnectionTaskToDefaultConnectionEventData extends ConnectionStrategyChangeEventData {
        protected SwitchFromPartialConnectionTaskToDefaultConnectionEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private PartialConnectionTask getPartialConnectionTask() {
            return getDeviceConfigurationService().findPartialConnectionTask(this.getLong("partialConnectionTaskId")).get();
        }
    }

    private static class SwitchFromPartialConnectionTaskToConnectionFunctionEventData extends ConnectionStrategyChangeEventData {
        protected SwitchFromPartialConnectionTaskToConnectionFunctionEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private PartialConnectionTask getPartialConnectionTask() {
            return getDeviceConfigurationService().findPartialConnectionTask(this.getLong("oldPartialConnectionTaskId")).get();
        }

        private ConnectionFunction getConnectionFunction() {
            return getConnectionFunction(getComTaskEnablement().getDeviceConfiguration(), this.getLong("newConnectionFunctionId"));
        }
    }

    private static class SwitchFromConnectionFunctionToDefaultConnectionEventData extends ConnectionStrategyChangeEventData {
        protected SwitchFromConnectionFunctionToDefaultConnectionEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private ConnectionFunction getConnectionFunction() {
            return getConnectionFunction(getComTaskEnablement().getDeviceConfiguration(), this.getLong("oldConnectionFunctionId"));
        }
    }

    private static class SwitchFromConnectionFunctionToPartialConnectionTaskEventData extends ConnectionStrategyChangeEventData {
        protected SwitchFromConnectionFunctionToPartialConnectionTaskEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private ConnectionFunction getConnectionFunction() {
            return getConnectionFunction(getComTaskEnablement().getDeviceConfiguration(), this.getLong("oldConnectionFunctionId"));
        }

        private PartialConnectionTask getPartialConnectionTask() {
            return getDeviceConfigurationService().findPartialConnectionTask(this.getLong("newPartialConnectionTaskId")).get();
        }
    }

    private static class SwitchOnUsingDefaultConnectionEventData extends ConnectionStrategyChangeEventData {
        protected SwitchOnUsingDefaultConnectionEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }
    }

    private static class SwitchOnUsingConnectionFunctionEventData extends ConnectionStrategyChangeEventData {
        protected SwitchOnUsingConnectionFunctionEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private ConnectionFunction getConnectionFunction() {
            return getConnectionFunction(getComTaskEnablement().getDeviceConfiguration(), this.getLong("newConnectionFunctionId"));
        }
    }

    private static class SwitchOffUsingDefaultConnectionEventData extends ConnectionStrategyChangeEventData {
        protected SwitchOffUsingDefaultConnectionEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }
    }

    private static class SwitchOffUsingConnectionFunctionEventData extends ConnectionStrategyChangeEventData {
        protected SwitchOffUsingConnectionFunctionEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private ConnectionFunction getConnectionFunction() {
            return getConnectionFunction(getComTaskEnablement().getDeviceConfiguration(), this.getLong("oldConnectionFunctionId"));
        }
    }

    private static class SwitchBetweenPartialConnectionTasksEventData extends ConnectionStrategyChangeEventData {
        protected SwitchBetweenPartialConnectionTasksEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private PartialConnectionTask getNewPartialConnectionTask() {
            return getDeviceConfigurationService().findPartialConnectionTask(this.getLong("newPartialConnectionTaskId")).get();
        }

        private PartialConnectionTask getOldPartialConnectionTask() {
            return getDeviceConfigurationService().findPartialConnectionTask(this.getLong("oldPartialConnectionTaskId")).get();
        }
    }

    private static class SwitchBetweenConnectionFunctionsEventData extends ConnectionStrategyChangeEventData {
        protected SwitchBetweenConnectionFunctionsEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private ConnectionFunction getOldConnectionFunction() {
            return getConnectionFunction(getComTaskEnablement().getDeviceConfiguration(), this.getLong("oldConnectionFunctionId"));
        }

        private ConnectionFunction getNewConnectionFunction() {
            return getConnectionFunction(getComTaskEnablement().getDeviceConfiguration(), this.getLong("newConnectionFunctionId"));
        }
    }

    private static class StartUsingPartialConnectionTaskEventData extends ConnectionStrategyChangeEventData {
        protected StartUsingPartialConnectionTaskEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private PartialConnectionTask getPartialConnectionTask() {
            return getDeviceConfigurationService().findPartialConnectionTask(this.getLong("partialConnectionTaskId")).get();
        }
    }

    private static class RemovePartialConnectionTaskEventData extends ConnectionStrategyChangeEventData {
        protected RemovePartialConnectionTaskEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private PartialConnectionTask getPartialConnectionTask() {
            return getDeviceConfigurationService().findPartialConnectionTask(this.getLong("partialConnectionTaskId")).get();
        }
    }

    private enum ActualEventHandler {
        SWITCH_ON_DEFAULT {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchOnUsingDefaultConnectionEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchOnUsingDefaultConnectionEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchOnDefault(comTask, deviceConfiguration);
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_ON_DEFAULT;
            }
        },
        SWITCH_ON_CONNECTION_FUNCTION {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchOnUsingConnectionFunctionEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchOnUsingConnectionFunctionEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();

                communicationTaskService.switchOnConnectionFunction(
                        comTask,
                        deviceConfiguration,
                        eventData.getConnectionFunction());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_ON_CONNECTION_FUNCTION;
            }
        },

        SWITCH_OFF_DEFAULT {
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchOffUsingDefaultConnectionEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchOffUsingDefaultConnectionEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchOffDefault(comTask, deviceConfiguration);
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_OFF_DEFAULT;
            }
        },

        SWITCH_OFF_CONNECTION_FUNCTION {
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchOffUsingConnectionFunctionEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchOffUsingConnectionFunctionEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchOffConnectionFunction(
                        comTask,
                        deviceConfiguration,
                        eventData.getConnectionFunction());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_OFF_CONNECTION_FUNCTION;
            }
        },

        SWITCH_FROM_DEFAULT_TO_TASK {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchFromDefaultConnectionToPartialConnectionTaskEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchFromDefaultConnectionToPartialConnectionTaskEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchFromDefaultConnectionTaskToPreferredConnectionTask(
                        comTask,
                        deviceConfiguration,
                        eventData.getPartialConnectionTask());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_TASK;
            }
        },

        SWITCH_FROM_DEFAULT_TO_CONNECTION_FUNCTION {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchFromDefaultConnectionToConnectionFunctionEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchFromDefaultConnectionToConnectionFunctionEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchFromDefaultConnectionTaskToConnectionFunction(
                        comTask,
                        deviceConfiguration,
                        eventData.getConnectionFunction());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_CONNECTION_FUNCTION;
            }
        },

        SWITCH_FROM_TASK_TO_DEFAULT {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchFromPartialConnectionTaskToDefaultConnectionEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchFromPartialConnectionTaskToDefaultConnectionEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchFromPreferredConnectionTaskToDefault(
                        comTask,
                        deviceConfiguration,
                        eventData.getPartialConnectionTask());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_DEFAULT;
            }
        },

        SWITCH_FROM_TASK_TO_CONNECTION_FUNCTION {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchFromPartialConnectionTaskToConnectionFunctionEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchFromPartialConnectionTaskToConnectionFunctionEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchFromPreferredConnectionTaskToConnectionFunction(
                        comTask,
                        deviceConfiguration,
                        eventData.getPartialConnectionTask(),
                        eventData.getConnectionFunction());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_CONNECTION_FUNCTION;
            }
        },

        SWITCH_FROM_CONNECTION_FUNCTION_TO_DEFAULT {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchFromConnectionFunctionToDefaultConnectionEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchFromConnectionFunctionToDefaultConnectionEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchFromConnectionFunctionToDefault(
                        comTask,
                        deviceConfiguration,
                        eventData.getConnectionFunction());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_FROM_CONNECTION_FUNCTION_TO_DEFAULT;
            }
        },

        SWITCH_FROM_CONNECTION_FUNCTION_TO_TASK {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchFromConnectionFunctionToPartialConnectionTaskEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchFromConnectionFunctionToPartialConnectionTaskEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchFromConnectionFunctionToPreferredConnectionTask(
                        comTask,
                        deviceConfiguration,
                        eventData.getConnectionFunction(),
                        eventData.getPartialConnectionTask());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_FROM_CONNECTION_FUNCTION_TO_TASK;
            }
        },

        SWITCH_BETWEEN_TASKS {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchBetweenPartialConnectionTasksEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchBetweenPartialConnectionTasksEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.preferredConnectionTaskChanged(
                        comTask,
                        deviceConfiguration,
                        eventData.getOldPartialConnectionTask(),
                        eventData.getNewPartialConnectionTask());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_BETWEEN_TASKS;
            }

        },

        SWITCH_BETWEEN_CONNECTION_FUNCTIONS {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new SwitchBetweenConnectionFunctionsEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(SwitchBetweenConnectionFunctionsEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.preferredConnectionFunctionChanged(
                        comTask,
                        deviceConfiguration,
                        eventData.getOldConnectionFunction(),
                        eventData.getNewConnectionFunction());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_SWITCH_BETWEEN_CONNECTION_FUNCTIONS;
            }
        },

        USE_TASK {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new StartUsingPartialConnectionTaskEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(StartUsingPartialConnectionTaskEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchOnPreferredConnectionTask(
                        comTask,
                        deviceConfiguration,
                        eventData.getPartialConnectionTask());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_START_USING_TASK;
            }
        },

        REMOVE_TASK {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new RemovePartialConnectionTaskEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(RemovePartialConnectionTaskEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.removePreferredConnectionTask(
                        comTask,
                        deviceConfiguration,
                        eventData.getPartialConnectionTask());
            }

            @Override
            protected EventType eventType() {
                return EventType.COMTASKENABLEMENT_REMOVE_TASK;
            }
        },

        DEV_NULL {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                // Designed to ignore everything
            }

            @Override
            protected String topic() {
                return "";
            }

            @Override
            protected EventType eventType() {
                return null;
            }
        };

        abstract void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator);

        protected String topic() {
            return this.eventType().topic();
        }

        protected abstract EventType eventType();

        private static ActualEventHandler forTopic(String topic) {
            Set<ActualEventHandler> candidates = EnumSet.range(SWITCH_ON_DEFAULT, REMOVE_TASK);
            for (ActualEventHandler actualEventHandler : candidates) {
                if (actualEventHandler.topic().equals(topic)) {
                    return actualEventHandler;
                }
            }
            return DEV_NULL;
        }

    }
}