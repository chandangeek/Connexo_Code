package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that all collected data in both load profiles and registers is valid.
 * The actual check is done by comparing the last reading timestamp
 * of all profiles and registers against the last checked.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-13 (11:24)
 */
public class AllDataValid extends TranslatableServerMicroCheck {

    private final ValidationService validationService;

    public AllDataValid(ValidationService validationService, Thesaurus thesaurus) {
        super(thesaurus);
        this.validationService = validationService;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        Optional<? extends MeterActivation> current = device.getCurrentMeterActivation();
        if (current.isPresent()) {
            if (validationService.validationEnabled(current.get().getMeter().get())) {
                return (this.validationService.getEvaluator().isAllDataValid(current.get()) ? Optional.empty() : Optional.of(newViolation()));
            }else{
                return Optional.empty();
            }
        }
        return Optional.of(newViolation());
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.ALL_DATA_VALID,
                MicroCheck.ALL_DATA_VALID);
    }


    @Override
    protected MicroCheck getMicroCheck() {
        return MicroCheck.ALL_DATA_VALID;
    }
}