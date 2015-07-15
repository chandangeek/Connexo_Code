package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.tasks.ComTask;
import org.osgi.service.event.EventConstants;

import java.util.EnumSet;
import java.util.Map;
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

    }

    private static class SwitchFromDefaultConnectionToPartialConnectionTaskEventData extends ConnectionStrategyChangeEventData {
        protected SwitchFromDefaultConnectionToPartialConnectionTaskEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }

        private PartialConnectionTask getPartialConnectionTask() {
            return getDeviceConfigurationService().findPartialConnectionTask(this.getLong("partialConnectionTaskId")).get();
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

    private static class SwitchOnUsingDefaultConnectionEventData extends ConnectionStrategyChangeEventData {
        protected SwitchOnUsingDefaultConnectionEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
        }
    }

    private static class SwitchOffUsingDefaultConnectionEventData extends ConnectionStrategyChangeEventData {
        protected SwitchOffUsingDefaultConnectionEventData(DeviceConfigurationService deviceConfigurationService, Map<String, Object> messageProperties) {
            super(deviceConfigurationService, messageProperties);
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
        },

        USE_TASK {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                this.process(new StartUsingPartialConnectionTaskEventData(serviceLocator.deviceConfigurationService(), messageProperties), serviceLocator.communicationTaskService());
            }

            private void process(StartUsingPartialConnectionTaskEventData eventData, ServerCommunicationTaskService communicationTaskService) {
                ComTask comTask = eventData.getComTaskEnablement().getComTask();
                DeviceConfiguration deviceConfiguration = eventData.getComTaskEnablement().getDeviceConfiguration();
                communicationTaskService.switchFromDefaultConnectionTaskToPreferredConnectionTask(
                        comTask,
                        deviceConfiguration,
                        eventData.getPartialConnectionTask());
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
        },

        DEV_NULL {
            @Override
            void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
                // Designed to ignore everything
            }
        };

        abstract void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator);

        private String topic() {
            return "com/energyict/mdc/device/config/comtaskenablement/" + this.name();
        }

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