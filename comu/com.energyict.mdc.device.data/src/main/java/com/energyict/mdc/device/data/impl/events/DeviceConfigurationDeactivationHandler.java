package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.ServerDeviceDataService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Handles delete events that are being sent when a {@link DeviceConfiguration}
 * is about to be deactivated and will veto the action when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-13 (16:09)
 */
@Component(name="com.energyict.mdc.device.data.deactivate.deviceconfiguration.eventhandler", service = TopicHandler.class, immediate = true)
public class DeviceConfigurationDeactivationHandler extends EventHandler<LocalEvent> {

    static final String TOPIC = "com/energyict/mdc/device/config/deviceconfiguration/VALIDATEDEACTIVATE";

    private volatile ServerDeviceDataService deviceDataService;
    private volatile Thesaurus thesaurus;

    public DeviceConfigurationDeactivationHandler() {
        super(LocalEvent.class);
    }

    @Inject
    DeviceConfigurationDeactivationHandler(ServerDeviceDataService deviceDataService, Thesaurus thesaurus) {
        this();
        this.deviceDataService = deviceDataService;
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = (ServerDeviceDataService) deviceDataService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(TOPIC)) {
            DeviceConfiguration deviceConfiguration = (DeviceConfiguration) event.getSource();
            this.validateNotUsedByDevice(deviceConfiguration);
        }
    }

    /**
     * Vetos the delection of the {@link DeviceConfiguration}
     * by throwing an exception when there is at least
     * one Device created from that DeviceConfiguration.
     *
     * @param deviceConfiguration The ComTaskEnablement that is about to be deleted
     */
    private void validateNotUsedByDevice(DeviceConfiguration deviceConfiguration) {
        if (this.deviceDataService.hasDevices(deviceConfiguration)) {
            throw new VetoDeactivateDeviceConfigurationException(this.thesaurus, deviceConfiguration);
        }
    }

}