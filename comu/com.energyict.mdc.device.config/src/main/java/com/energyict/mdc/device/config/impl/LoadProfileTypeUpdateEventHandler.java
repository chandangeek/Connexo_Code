package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.exceptions.CannotUpdateIntervalWhenLoadProfileTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenLoadProfileTypeIsInUseException;
import com.energyict.mdc.masterdata.LoadProfileType;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;

import java.util.List;

import static com.elster.jupiter.util.Checks.is;

/**
 * Handles update events that are being sent when a {@link LoadProfileType}
 * has been updated and will veto the update when:
 * <ul>
 * <li>that would cause a duplicate {@link com.energyict.mdc.common.ObisCode}
 * in a {@link DeviceConfiguration} that is using the LoadProfileType.</li>
 * <li>the ObisCode changed and the LoadProfileType is already in use by a {@link LoadProfileSpec}</li>
 * <li>the interval changed and the LoadProfileType is already in use by a {@link LoadProfileSpec}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (17:02)
 */
@Component(name="com.energyict.mdc.device.config.loadprofiletype.update.eventhandler", service = TopicHandler.class, immediate = true)
public class LoadProfileTypeUpdateEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/masterdata/loadprofiletype/UPDATED";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public LoadProfileTypeUpdateEventHandler() {
        super();
    }

    public LoadProfileTypeUpdateEventHandler(DeviceConfigurationService deviceConfigurationService) {
        this();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        LoadProfileType loadProfileType = (LoadProfileType) event.getSource();
        this.validateDeviceConfigurations(loadProfileType);
        if (this.isUsed(loadProfileType)) {
            if (this.obisCodeChanged(event, loadProfileType)) {
                throw new CannotUpdateObisCodeWhenLoadProfileTypeIsInUseException(loadProfileType, this.thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_OBIS_CODE_CANNOT_BE_UPDATED);
            }
            if (this.intervalChanged(event, loadProfileType)) {
                throw new CannotUpdateIntervalWhenLoadProfileTypeIsInUseException(loadProfileType, this.thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_CANNOT_BE_UPDATED);
            }
        }
    }

    private void validateDeviceConfigurations(LoadProfileType loadProfileType) {
        List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingLoadProfileType(loadProfileType);
        for (DeviceConfiguration each : deviceConfigurations) {
            ServerDeviceConfiguration deviceConfiguration = (ServerDeviceConfiguration) each;
            deviceConfiguration.validateUpdateLoadProfileType(loadProfileType);
        }
    }

    private boolean obisCodeChanged(LocalEvent event, LoadProfileType loadProfileType) {
        Event osgiEvent = event.toOsgiEvent();
        return osgiEvent.containsProperty("oldObisCode")
                && !is(osgiEvent.getProperty("oldObisCode")).equalTo(loadProfileType.getObisCode().toString());
    }

    private boolean intervalChanged(LocalEvent event, LoadProfileType loadProfileType) {
        Event osgiEvent = event.toOsgiEvent();
        Object oldIntervalSeconds = osgiEvent.getProperty("oldIntervalSeconds");
        return oldIntervalSeconds != null && !((Long) oldIntervalSeconds == loadProfileType.getInterval().getSeconds());
    }

    private boolean isUsed(LoadProfileType loadProfileType) {
        return this.usedByLoadProfileSpecs(loadProfileType);
    }

    private boolean usedByLoadProfileSpecs(LoadProfileType loadProfileType) {
        List<LoadProfileSpec> loadProfileSpecs = this.deviceConfigurationService.findLoadProfileSpecsByLoadProfileType(loadProfileType);
        return !loadProfileSpecs.isEmpty();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(DeviceConfigurationService.COMPONENTNAME, Layer.DOMAIN));
    }

    private void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

}