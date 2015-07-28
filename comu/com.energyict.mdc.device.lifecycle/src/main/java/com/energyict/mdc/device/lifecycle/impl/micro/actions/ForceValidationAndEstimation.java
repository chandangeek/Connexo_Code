package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.exception.BaseException;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will force validation and estimation on channels and registers.
 * A runtime error is thrown when the device is not set for validation and/or estimation.
 * When after validation and estimation still invalid values are encountered, the action is undone.
 *
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#FORCE_VALIDATION_AND_ESTIMATION}
 */
public class ForceValidationAndEstimation extends TranslatableServerMicroAction {

    //Required services
    private final ValidationService validationService;
    private final EstimationService estimationService;

    private Device device;

    public ForceValidationAndEstimation(Thesaurus thesaurus, ValidationService validationService, EstimationService estimationService) {
        super(thesaurus);
        this.validationService = validationService;
        this.estimationService = estimationService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        // Remember that effective timestamp is a required property enforced by the service's execute method
        return Collections.emptyList();
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        this.device = device;
        if (this.device.forValidation().isValidationActive() && this.device.forEstimation().isEstimationActive()) {
            this.device.getLoadProfiles().forEach(loadProfile -> this.setLastReading(loadProfile, effectiveTimestamp));
            this.device.getCurrentMeterActivation().ifPresent(this::validateAndEstimate);
        }
    }

    private void setLastReading(LoadProfile loadProfile, Instant newlastReading) {
        device.getLoadProfileUpdaterFor(loadProfile).setLastReadingIfLater(newlastReading).update();
    }

    private void validateAndEstimate(MeterActivation meterActivation) {
        validationService.validate(meterActivation);
        estimationService.estimate(meterActivation, meterActivation.getRange());
        long nrOfSuspects = meterActivation.getChannels().stream()
                .flatMap(channel ->
                        channel.findActualReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), Range.<Instant>all()).stream())
                .count();
        if (nrOfSuspects > 0) {
            throw new ForceValidationAndEstimationException(MessageSeeds.NOT_ALL_DATA_VALID_FOR_DEVICE);
        }
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.FORCE_VALIDATION_AND_ESTIMATION;
    }

    public class ForceValidationAndEstimationException extends BaseException {
        protected ForceValidationAndEstimationException(MessageSeed messageSeed) {
            super(messageSeed, device.getName());
        }
    }
}
