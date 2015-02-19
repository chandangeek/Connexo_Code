package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperty;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Handles delete events that are being sent when a single property of a {@link ProtocolDialectConfigurationProperties}
 * is about to be deleted and will veto the action when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-18 (16:34)
 */
@Component(name = "com.energyict.mdc.device.data.delete.deviceconfiguration.protocoldialect.value.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class ProtocolDialectConfigurationPropertyValueDeletionHandler implements TopicHandler {

    static final String TOPIC = "com/energyict/mdc/device/config/protocolconfigurationprops/VALIDATE_REMOVE_ONE";

    private volatile DeviceDataModelService deviceDataModelService;
    private Thesaurus thesaurus;

    // For OSGi framework
    public ProtocolDialectConfigurationPropertyValueDeletionHandler() {
        super();
    }

    // For testing purposes only
    ProtocolDialectConfigurationPropertyValueDeletionHandler(DeviceDataModelService deviceDataModelService) {
        this();
        this.setDeviceDataModelService(deviceDataModelService);
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
        this.thesaurus = deviceDataModelService.thesaurus();
    }

    /**
     * Vetos the deletion of the {@link ProtocolDialectConfigurationProperties}
     * by throwing an exception when there is at least
     * one Device that is overruling properties of the same dialect.
     *
     * @param property The ProtocolDialectConfigurationProperties that is about to be deleted
     */
    private void validateNotUsedByDevice(ProtocolDialectConfigurationProperty property) {
        ProtocolDialectConfigurationProperties configurationProperties = property.getProtocolDialectConfigurationProperties();
        PropertySpec propertySpec = configurationProperties.getDeviceProtocolDialect().getPropertySpec(property.getName());
        if (propertySpec != null && propertySpec.isRequired()) {
            if (this.deviceDataModelService.deviceService().hasDevices(configurationProperties, propertySpec)) {
                throw new VetoDeleteProtocolDialectConfigurationPropertyException(this.thesaurus, property);
            }
        }
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.validateNotUsedByDevice((ProtocolDialectConfigurationProperty) localEvent.getSource());
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

}