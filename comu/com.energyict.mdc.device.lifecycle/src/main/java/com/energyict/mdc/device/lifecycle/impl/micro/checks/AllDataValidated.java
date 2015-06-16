package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import java.time.Instant;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 16/06/2015
 * Time: 14:17
 */
public class AllDataValidated implements ServerMicroCheck {

    private final ValidationService validationService;
    private final Thesaurus thesaurus;

    public AllDataValidated(ValidationService validationService, Thesaurus thesaurus) {
        super();
        this.validationService = validationService;
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        Optional<? extends MeterActivation> current = device.getCurrentMeterActivation();
        if (current.isPresent()) {
            if (validationService.validationEnabled(current.get().getMeter().get())) {
                return (this.validationService.getEvaluator().isAllDataValidated(current.get()) ? Optional.empty() : Optional.of(newViolation()));
            }else{
                return Optional.empty();
            }
        }
        return Optional.of(newViolation());
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.ALL_DATA_VALIDATED,
                MicroCheck.ALL_DATA_VALIDATED);
    }

}
