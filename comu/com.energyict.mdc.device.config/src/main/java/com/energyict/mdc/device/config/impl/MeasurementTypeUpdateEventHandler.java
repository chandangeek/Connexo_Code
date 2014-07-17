package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenMeasurementTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdatePhenomenonWhenMeasurementTypeIsInUseException;
import com.energyict.mdc.masterdata.MeasurementType;

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
 * <li>the {@link Phenomenon} changed and the MeasurementType is already in use by a {@link ChannelSpec} or a {@link RegisterSpec}</li>
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

    public MeasurementTypeUpdateEventHandler() {
        super();
    }

    public MeasurementTypeUpdateEventHandler(DeviceConfigurationService deviceConfigurationService) {
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
        this.validateDeviceConfigurations(measurementType);
        if (this.isUsed(measurementType)) {
            if (this.obisCodeChanged(event, measurementType)) {
                throw new CannotUpdateObisCodeWhenMeasurementTypeIsInUseException(this.thesaurus, measurementType);
            }
            if (this.phenomenonChanged(event, measurementType)) {
                throw new CannotUpdatePhenomenonWhenMeasurementTypeIsInUseException(this.thesaurus, measurementType);
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

    private boolean phenomenonChanged(LocalEvent event, MeasurementType measurementType) {
        Event osgiEvent = event.toOsgiEvent();
        return osgiEvent.containsProperty("oldPhenomenon")
                && !is(osgiEvent.getProperty("oldPhenomenon")).equalTo(measurementType.getPhenomenon().getId());
    }

    private boolean isUsed(MeasurementType measurementType) {
        return this.usedByChannelSpecs(measurementType) || this.usedByRegisterSpecs(measurementType);
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

}