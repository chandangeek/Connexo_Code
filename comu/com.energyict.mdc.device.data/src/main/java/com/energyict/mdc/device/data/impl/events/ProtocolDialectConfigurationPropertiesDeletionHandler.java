package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Handles delete events that are being sent when a {@link ProtocolDialectConfigurationProperties}
 * is about to be deleted and will veto the action when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-18 (15:55)
 */
@Component(name = "com.energyict.mdc.device.data.delete.deviceconfiguration.protocoldialect.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class ProtocolDialectConfigurationPropertiesDeletionHandler implements TopicHandler {

    static final String TOPIC = "com/energyict/mdc/device/config/protocolconfigurationprops/VALIDATEDELETE";

    private volatile DeviceDataModelService deviceDataModelService;
    private Thesaurus thesaurus;

    // For OSGi framework
    public ProtocolDialectConfigurationPropertiesDeletionHandler() {
        super();
    }

    // For testing purposes only
    ProtocolDialectConfigurationPropertiesDeletionHandler(DeviceDataModelService deviceDataModelService) {
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
     * @param configurationProperties The ProtocolDialectConfigurationProperties that is about to be deleted
     */
    private void validateNotUsedByDevice(ProtocolDialectConfigurationProperties configurationProperties) {
        if (this.deviceDataModelService.deviceService().hasDevices(configurationProperties)) {
            throw new VetoDeleteProtocolDialectConfigurationPropertiesException(this.thesaurus, configurationProperties);
        }
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.validateNotUsedByDevice((ProtocolDialectConfigurationProperties) localEvent.getSource());
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

}