package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.MeasurementType;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Handles delete events that are being sent when a {@link com.energyict.mdc.masterdata.MeasurementType}
 * is about to be deleted and will veto the delete when the MeasurementType is still used by:
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
@Component(name="com.energyict.mdc.device.config.measurementtype.delete.eventhandler", service = TopicHandler.class, immediate = true)
public class MeasurementTypeDeletionEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/masterdata/measurementtype/VALIDATEDELETE";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public MeasurementTypeDeletionEventHandler() {
        super();
    }

    public MeasurementTypeDeletionEventHandler(DeviceConfigurationService deviceConfigurationService) {
        this();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        MeasurementType measurementType = (MeasurementType) event.getSource();
        this.validateNotUsedByRegisterSpecs(measurementType);
        this.validateNotUsedByChannelSpecs(measurementType);
        this.validateNotUsedByDeviceTypes(measurementType);
    }

    private void validateNotUsedByRegisterSpecs(MeasurementType measurementType) {
        List<RegisterSpec> registerSpecs = this.deviceConfigurationService.findRegisterSpecsByMeasurementType(measurementType);
        if (!registerSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerTypeIsStillInUseByRegisterSpecs(this.thesaurus, measurementType, registerSpecs, MessageSeeds.REGISTER_TYPE_STILL_USED_BY_REGISTER_SPEC);
        }
    }

    private void validateNotUsedByChannelSpecs(MeasurementType measurementType) {
        List<ChannelSpec> channelSpecs = this.deviceConfigurationService.findChannelSpecsForMeasurementType(measurementType);
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.channelTypeIsStillInUseByChannelSpecs(this.thesaurus, measurementType, channelSpecs, MessageSeeds.CHANNEL_TYPE_STILL_USED_BY_CHANNEL_SPEC);
        }
    }

    private void validateNotUsedByDeviceTypes(MeasurementType measurementType) {
        List<DeviceType> deviceTypes = this.deviceConfigurationService.findDeviceTypesUsingRegisterType(measurementType);
        if (!deviceTypes.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerTypeIsStillInUseByDeviceTypes(this.thesaurus, measurementType, deviceTypes, MessageSeeds.REGISTER_TYPE_STILL_USED_BY_DEVICE_TYPE);
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