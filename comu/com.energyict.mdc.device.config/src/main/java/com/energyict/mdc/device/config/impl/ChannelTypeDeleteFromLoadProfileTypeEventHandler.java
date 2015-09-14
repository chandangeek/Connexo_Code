package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeChannelTypeUsage;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Handles events that are being sent when a {@link com.energyict.mdc.masterdata.MeasurementType} is being
 * deleted from a {@link LoadProfileType}
 * and will veto the delete when the ChannelType is still used by:
 * <ul>
 * <li>a {@link ChannelSpec}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (17:44)
 */
@Component(name="com.energyict.mdc.device.config.channeltype.in.loadprofiletype.delete.eventhandler", service = TopicHandler.class, immediate = true)
public class ChannelTypeDeleteFromLoadProfileTypeEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/masterdata/channeltypeinloadprofiletype/VALIDATEDELETE";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public ChannelTypeDeleteFromLoadProfileTypeEventHandler() {
        super();
    }

    public ChannelTypeDeleteFromLoadProfileTypeEventHandler(DeviceConfigurationService deviceConfigurationService) {
        this();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        LoadProfileTypeChannelTypeUsage channelTypeUsage = (LoadProfileTypeChannelTypeUsage) event.getSource();
        this.validateNoChannelSpecForChannelType(channelTypeUsage);
    }

    private void validateNoChannelSpecForChannelType(LoadProfileTypeChannelTypeUsage channelTypeUsage) {
        List<ChannelSpec> channelSpecs = this.deviceConfigurationService.findChannelSpecsForChannelTypeInLoadProfileType(channelTypeUsage.getChannelType(), channelTypeUsage.getLoadProfileType());
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.channelTypeIsStillInUseByChannelSpecs(this.thesaurus, channelTypeUsage.getChannelType(), channelSpecs, MessageSeeds.CHANNEL_TYPE_STILL_USED_BY_CHANNEL_SPEC);
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