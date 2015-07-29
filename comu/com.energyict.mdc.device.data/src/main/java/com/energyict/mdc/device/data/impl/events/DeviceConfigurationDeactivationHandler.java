package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Handles delete events that are being sent when a {@link DeviceConfiguration}
 * is about to be deactivated and will veto the action when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-13 (16:09)
 */
@Component(name = "com.energyict.mdc.device.data.deactivate.deviceconfiguration.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceConfigurationDeactivationHandler implements TopicHandler {

    private static final String TOPIC = EventType.DEVICECONFIGURATION_VALIDATEDEACTIVATE.topic();

    private volatile DeviceDataModelService deviceDataModelService;
    private Thesaurus thesaurus;

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
        this.thesaurus = deviceDataModelService.thesaurus();
    }

    /**
     * Vetos the deletion of the {@link DeviceConfiguration}
     * by throwing an exception when there is at least
     * one Device created from that DeviceConfiguration.
     *
     * @param deviceConfiguration The ComTaskEnablement that is about to be deleted
     */
    private void validateNotUsedByDevice(DeviceConfiguration deviceConfiguration) {
        if (this.deviceDataModelService.deviceService().hasDevices(deviceConfiguration)) {
            throw new VetoDeactivateDeviceConfigurationException(this.thesaurus, deviceConfiguration);
        }
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceConfiguration deviceConfiguration = (DeviceConfiguration) localEvent.getSource();
        this.validateNotUsedByDevice(deviceConfiguration);
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }
}