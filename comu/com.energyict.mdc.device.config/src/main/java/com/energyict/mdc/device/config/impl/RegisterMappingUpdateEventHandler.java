package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdatePhenomenonWhenRegisterMappingIsInUseException;
import com.energyict.mdc.masterdata.RegisterMapping;

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
 * Handles update events that are being sent when a {@link com.energyict.mdc.masterdata.RegisterMapping}
 * has been updated and will veto the update when:
 * <ul>
 * <li>that would cause a duplicate {@link com.energyict.mdc.common.ObisCode}
 * in a {@link DeviceConfiguration} that is using the RegisterMapping.</li>
 * <li>the ObisCode changed and the RegisterMapping is already in use by a {@link ChannelSpec} or a {@link RegisterSpec}</li>
 * <li>the {@link Phenomenon} changed and the RegisterMapping is already in use by a {@link ChannelSpec} or a {@link RegisterSpec}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (15:22)
 */
@Component(name="com.energyict.mdc.device.config.registermapping.update.eventhandler", service = TopicHandler.class, immediate = true)
public class RegisterMappingUpdateEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/masterdata/registermapping/UPDATED";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public RegisterMappingUpdateEventHandler(DeviceConfigurationService deviceConfigurationService) {
        super();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        RegisterMapping registerMapping = (RegisterMapping) event.getSource();
        this.validateDeviceConfigurations(registerMapping);
        if (this.isUsed(registerMapping)) {
            if (this.obisCodeChanged(event, registerMapping)) {
                throw new CannotUpdateObisCodeWhenRegisterMappingIsInUseException(this.thesaurus, registerMapping);
            }
            if (this.phenomenonChanged(event, registerMapping)) {
                throw new CannotUpdatePhenomenonWhenRegisterMappingIsInUseException(this.thesaurus, registerMapping);
            }
        }
    }

    private void validateDeviceConfigurations(RegisterMapping registerMapping) {
        List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingRegisterMapping(registerMapping);
        for (DeviceConfiguration each : deviceConfigurations) {
            ServerDeviceConfiguration deviceConfiguration = (ServerDeviceConfiguration) each;
            deviceConfiguration.validateUpdateRegisterMapping(registerMapping);
        }
    }

    private boolean obisCodeChanged(LocalEvent event, RegisterMapping registerMapping) {
        Event osgiEvent = event.toOsgiEvent();
        return osgiEvent.containsProperty("oldObisCode")
                && !is(osgiEvent.getProperty("oldObisCode")).equalTo(registerMapping.getObisCode().toString());
    }

    private boolean phenomenonChanged(LocalEvent event, RegisterMapping registerMapping) {
        Event osgiEvent = event.toOsgiEvent();
        return osgiEvent.containsProperty("oldPhenomenon")
                && !is(osgiEvent.getProperty("oldPhenomenon")).equalTo(registerMapping.getPhenomenon().getId());
    }

    private boolean isUsed(RegisterMapping registerMapping) {
        return this.usedByChannelSpecs(registerMapping) || this.usedByRegisterSpecs(registerMapping);
    }

    private boolean usedByChannelSpecs(RegisterMapping registerMapping) {
        List<ChannelSpec> channelSpecs = this.deviceConfigurationService.findChannelSpecsForRegisterMapping(registerMapping);
        return !channelSpecs.isEmpty();
    }

    private boolean usedByRegisterSpecs(RegisterMapping registerMapping) {
        List<RegisterSpec> registerSpecs = this.deviceConfigurationService.findRegisterSpecsByRegisterMapping(registerMapping);
        return !registerSpecs.isEmpty();
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