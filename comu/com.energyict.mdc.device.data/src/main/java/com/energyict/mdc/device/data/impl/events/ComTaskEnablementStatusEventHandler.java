package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.ServerDeviceDataService;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;

/**
 * Handles events that are being sent when a {@link ComTaskEnablement}
 * was suspended or resumed.<br>
 * The changes that are being monitored are listed and described
 * <a href="http://confluence.eict.vpdc/display/JUPMDC/ComTaskEnablement+events">here</a>.
 * @see ComTaskEnablement#suspend()
 * @see ComTaskEnablement#resume()
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (11:51)
 */
@Component(name="com.energyict.mdc.device.data.update.comtaskenablement.status.messagehandler", service = MessageHandler.class, immediate = true)
public class ComTaskEnablementStatusEventHandler implements MessageHandler {

    private volatile JsonService jsonService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ServerDeviceDataService deviceDataService;

    public ComTaskEnablementStatusEventHandler() {
        super();
    }

    // For testing purposes
    ComTaskEnablementStatusEventHandler(JsonService jsonService, DeviceConfigurationService deviceConfigurationService, ServerDeviceDataService deviceDataService) {
        this();
        this.setJsonService(jsonService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setDeviceDataService(deviceDataService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.setDeviceDataService((ServerDeviceDataService) deviceDataService);
    }

    private void setDeviceDataService(ServerDeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
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
        public ServerDeviceDataService deviceDataService();
    }

    private class ServiceLocatorImpl implements ComTaskEnablementStatusEventHandler.ServiceLocator {
        @Override
        public DeviceConfigurationService deviceConfigurationService() {
            return deviceConfigurationService;
        }

        @Override
        public ServerDeviceDataService deviceDataService() {
            return deviceDataService;
        }
    }

    private enum ActualEventHandler {
        SUSPEND {
            @Override
            protected void process(Long comTaskEnablementId, ServiceLocator serviceLocator) {
                ComTaskEnablement comTaskEnablement = serviceLocator.deviceConfigurationService().findComTaskEnablement(comTaskEnablementId).get();
                serviceLocator.deviceDataService().suspendAll(comTaskEnablement.getComTask(), comTaskEnablement.getDeviceConfiguration());
            }
        },

        RESUME {
            @Override
            protected void process(Long comTaskEnablementId, ServiceLocator serviceLocator) {
                ComTaskEnablement comTaskEnablement = serviceLocator.deviceConfigurationService().findComTaskEnablement(comTaskEnablementId).get();
                serviceLocator.deviceDataService().resumeAll(comTaskEnablement.getComTask(), comTaskEnablement.getDeviceConfiguration());
            }
        },

        DEV_NULL {
            @Override
            protected void process(Long comTaskEnablementId, ServiceLocator serviceLocator) {
                // Designed to ignore everything
            }
        };

        protected void process(Map<String, Object> messageProperties, ServiceLocator serviceLocator) {
            Long comTaskEnablementId = this.getLong("id", messageProperties);
            this.process(comTaskEnablementId, serviceLocator);
        }

        protected abstract void process(Long comTaskEnablementId, ServiceLocator serviceLocator);

        private String topic () {
            return "com/energyict/mdc/device/config/comtaskenablement/" + this.name();
        }

        private Long getLong(String key, Map<String, Object> messageProperties) {
            if (messageProperties.containsKey(key)) {
                Object contents = messageProperties.get(key);
                if (contents instanceof Long) {
                    return (Long) contents;
                }
                else {
                    return ((Integer) contents).longValue();
                }
            }
            else {
                return null;
            }
        }

        private static ActualEventHandler forTopic (String topic) {
            Set<ActualEventHandler> candidates = EnumSet.range(SUSPEND, RESUME);
            for (ActualEventHandler actualEventHandler : candidates) {
                if (actualEventHandler.topic().equals(topic)) {
                    return actualEventHandler;
                }
            }
            return DEV_NULL;
        }

    }
}