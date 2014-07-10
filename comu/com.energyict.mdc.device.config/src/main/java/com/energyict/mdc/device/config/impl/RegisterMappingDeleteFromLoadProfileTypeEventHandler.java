package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeRegisterMappingUsage;
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
 * Handles events that are being sent when a {@link RegisterMapping} is being
 * deleted from a {@link LoadProfileType}
 * and will veto the delete when the RegisterMapping is still used by:
 * <ul>
 * <li>a {@link ChannelSpec}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (17:44)
 */
@Component(name="com.energyict.mdc.device.config.registermapping.in.loadprofiletype.delete.eventhandler", service = TopicHandler.class, immediate = true)
public class RegisterMappingDeleteFromLoadProfileTypeEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/masterdata/registermappinginloadprofiletype/VALIDATEDELETE";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public RegisterMappingDeleteFromLoadProfileTypeEventHandler(DeviceConfigurationService deviceConfigurationService) {
        super();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        LoadProfileTypeRegisterMappingUsage registerMappingUsage = (LoadProfileTypeRegisterMappingUsage) event.getSource();
        this.validateNoChannelSpecForRegisterMapping(registerMappingUsage);
    }

    private void validateNoChannelSpecForRegisterMapping(LoadProfileTypeRegisterMappingUsage registerMappingUsage) {
        List<ChannelSpec> channelSpecs = this.deviceConfigurationService.findChannelSpecsForRegisterMappingInLoadProfileType(registerMappingUsage.getRegisterMapping(), registerMappingUsage.getLoadProfileType());
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByChannelSpecs(this.thesaurus, registerMappingUsage.getRegisterMapping(), channelSpecs);
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