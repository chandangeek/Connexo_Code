package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenMeasurementTypeIsInUseException;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;

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
 * Handles update events that are being sent when a {@link com.energyict.mdc.masterdata.MeasurementType}
 * has been updated and will veto the update when:
 * <ul>
 * <li>that would cause a duplicate {@link com.energyict.mdc.common.ObisCode}
 * in a {@link DeviceConfiguration} that is using the MeasurementType.</li>
 * <li>the ObisCode changed and the MeasurementType is already in use by a {@link ChannelSpec} or a {@link RegisterSpec}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (15:22)
 */
@Component(name="com.energyict.mdc.device.config.measurementtype.update.eventhandler", service = TopicHandler.class, immediate = true)
public class MeasurementTypeUpdateEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/masterdata/measurementtype/UPDATED";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile MasterDataService masterDataService;

    public MeasurementTypeUpdateEventHandler() {
        super();
    }

    public MeasurementTypeUpdateEventHandler(DeviceConfigurationService deviceConfigurationService, MasterDataService masterDataService) {
        this();
        this.deviceConfigurationService = deviceConfigurationService;
        this.masterDataService = masterDataService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        MeasurementType measurementType = (MeasurementType) event.getSource();
        this.validateDeviceConfigurations(measurementType);
        if (this.isUsed(measurementType)) {
            if (this.obisCodeChanged(event, measurementType)) {
                throw new CannotUpdateObisCodeWhenMeasurementTypeIsInUseException(this.thesaurus, measurementType, MessageSeeds.MEASUREMENT_TYPE_OBIS_CODE_CANNOT_BE_UPDATED);
            }
        }
    }

    private void validateDeviceConfigurations(MeasurementType measurementType) {
        List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingMeasurementType(measurementType);
        for (DeviceConfiguration each : deviceConfigurations) {
            ServerDeviceConfiguration deviceConfiguration = (ServerDeviceConfiguration) each;
            deviceConfiguration.validateUpdateMeasurementTypes(measurementType);
        }
    }

    private boolean obisCodeChanged(LocalEvent event, MeasurementType measurementType) {
        Event osgiEvent = event.toOsgiEvent();
        return osgiEvent.containsProperty("oldObisCode")
                && !is(osgiEvent.getProperty("oldObisCode")).equalTo(measurementType.getObisCode().toString());
    }

    private boolean isUsed(MeasurementType measurementType) {
        return this.usedByChannelSpecs(measurementType) || this.usedByRegisterSpecs(measurementType) || this.usedByChannelTypeAsTemplate(measurementType);
    }

    private boolean usedByChannelTypeAsTemplate(MeasurementType measurementType) {
        return RegisterType.class.isAssignableFrom(measurementType.getClass()) && !this.masterDataService.findChannelTypeByTemplateRegister((RegisterType) measurementType).isEmpty();
    }

    private boolean usedByChannelSpecs(MeasurementType measurementType) {
        List<ChannelSpec> channelSpecs = this.deviceConfigurationService.findChannelSpecsForMeasurementType(measurementType);
        return !channelSpecs.isEmpty();
    }

    private boolean usedByRegisterSpecs(MeasurementType measurementType) {
        List<RegisterSpec> registerSpecs = this.deviceConfigurationService.findRegisterSpecsByMeasurementType(measurementType);
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

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }
}