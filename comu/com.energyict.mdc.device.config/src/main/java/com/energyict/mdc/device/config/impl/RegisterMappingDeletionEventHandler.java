package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.RegisterMapping;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Handles delete events that are being sent when a {@link RegisterMapping}
 * is about to be deleted and will veto the delete when the RegisterMapping is still used by:
 * <ul>
 * <li>a {@link com.energyict.mdc.device.config.DeviceType}
 * <li>a {@link com.energyict.mdc.device.config.RegisterSpec}
 * <li>a {@link com.energyict.mdc.device.config.ChannelSpec}
 * <li>a {@link com.energyict.mdc.device.config.LoadProfileSpec}
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (16:35)
 */
@Component(name="com.energyict.mdc.device.config.registermapping.delete.eventhandler", service = TopicHandler.class, immediate = true)
public class RegisterMappingDeletionEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/masterdata/registermapping/VALIDATEDELETE";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public RegisterMappingDeletionEventHandler(DeviceConfigurationService deviceConfigurationService) {
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
        this.validateNotUsedByRegisterSpecs(registerMapping);
        this.validateNotUsedByChannelSpecs(registerMapping);
        this.validateNotUsedByDeviceTypes(registerMapping);
    }

    private void validateNotUsedByRegisterSpecs(RegisterMapping registerMapping) {
        List<RegisterSpec> registerSpecs = this.deviceConfigurationService.findRegisterSpecsByRegisterMapping(registerMapping);
        if (!registerSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByRegisterSpecs(this.thesaurus, registerMapping, registerSpecs);
        }
    }

    private void validateNotUsedByChannelSpecs(RegisterMapping registerMapping) {
        List<ChannelSpec> channelSpecs = this.deviceConfigurationService.findChannelSpecsForRegisterMapping(registerMapping);
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByChannelSpecs(this.thesaurus, registerMapping, channelSpecs);
        }
    }

    private void validateNotUsedByDeviceTypes(RegisterMapping registerMapping) {
        List<DeviceType> deviceTypes = this.deviceConfigurationService.findDeviceTypesUsingRegisterMapping(registerMapping);
        if (!deviceTypes.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByDeviceTypes(this.thesaurus, registerMapping, deviceTypes);
        }
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