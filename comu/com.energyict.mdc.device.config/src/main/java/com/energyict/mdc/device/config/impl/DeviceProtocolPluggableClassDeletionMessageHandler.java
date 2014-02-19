package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.exceptions.VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.osgi.service.event.EventConstants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Handles delete events that are being sent when a {@link DeviceProtocolPluggableClass}
 * is being deleted and will check if a {@link DeviceType} is using it.
 * If that is the case, the delete will be vetoed by throwing an exception.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-19 (12:42)
 */
public class DeviceProtocolPluggableClassDeletionMessageHandler implements MessageHandler {

    private final JsonService jsonService;
    private final Thesaurus thesaurus;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ProtocolPluggableService protocolPluggableService;

    public DeviceProtocolPluggableClassDeletionMessageHandler(DeviceConfigurationService deviceConfigurationService, ProtocolPluggableService protocolPluggableService, NlsService nlsService, JsonService jsonService) {
        super();
        this.deviceConfigurationService = deviceConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.thesaurus = nlsService.getThesaurus(DeviceConfigurationService.COMPONENTNAME, Layer.DOMAIN);
        this.jsonService = jsonService;
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = this.jsonService.deserialize(message.getPayload(), Map.class);
        Object topic = map.get(EventConstants.EVENT_TOPIC);
        if ("com/energyict/mdc/protocol/pluggable/deviceprotocol/DELETED".equals(topic)) {
            this.handleDeleteMessage(map);
        }
    }

    private void handleDeleteMessage(Map<?,?> messageProperties) {
        Long deviceProtocolPluggableClassId = this.getLongFrom(messageProperties, "id");
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId);
        if (deviceProtocolPluggableClass != null) {
            List<DeviceType> deviceTypes = this.deviceConfigurationService.findDeviceTypesWithDeviceProtocol(deviceProtocolPluggableClass);
            if (!deviceTypes.isEmpty()) {
                throw new VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException(this.thesaurus, deviceProtocolPluggableClass, deviceTypes);
            }
        }
    }

    private Long getLongFrom (Map<?, ?> messageProperties, String propertyName) {
        Object contents = this.getPropertyFrom(messageProperties, propertyName);
        if (contents instanceof Long) {
            return (Long) contents;
        }
        else if (contents instanceof Integer) {
            return ((Integer) contents).longValue();
        }
        else if (contents instanceof BigDecimal) {
            return ((BigDecimal) contents).longValue();
        }
        else if (contents instanceof String) {
            return Long.parseLong((String) contents);
        }
        else {
            throw new IllegalArgumentException("Device protocol pluggable class id is expected to be numerial but got " + contents.getClass().getName());
        }
    }

    private Object getPropertyFrom (Map<?, ?> messageProperties, String propertyName) {
        Object propertyValue = messageProperties.get(propertyName);
        if (propertyValue == null) {
            throw new NullPointerException("Expected message parameters of device protocol pluggable class delete event to contain a value for property " + propertyName);
        }
        else {
            return propertyValue;
        }

    }

}