package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidator;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(name = "com.elster.jupiter.metering.impl.config.MeterActivationValidatorsWhiteboard", service = {MeterActivationValidatorsWhiteboard.class})
public class MeterActivationValidatorsWhiteboard {
    private List<CustomUsagePointMeterActivationValidator> customValidators = new CopyOnWriteArrayList<>();

    @SuppressWarnings("unused")
    public MeterActivationValidatorsWhiteboard() {
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCustomUsagePointMeterActivationValidator(CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator) {
        this.customValidators.add(customUsagePointMeterActivationValidator);
    }

    public void removeCustomUsagePointMeterActivationValidator(CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator) {
        this.customValidators.remove(customUsagePointMeterActivationValidator);
    }

    public void validateUsagePointMeterActivation(MeterRole meterRole, Meter meter, UsagePoint usagePoint) throws
            CustomUsagePointMeterActivationValidationException {
        this.customValidators.stream().forEach(validator -> validator.validateActivation(meterRole, meter, usagePoint));
    }
}
