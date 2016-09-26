package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.templates.ValidationRuleSetTpl;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import java.util.Optional;

public class CreateValidationDeviceCommand extends CreateDeviceCommand {

    private final ValidationService validationService;
    private final MeteringService meteringService;

    @Inject
    public CreateValidationDeviceCommand(ValidationService validationService, MeteringService meteringService) {
        this.validationService = validationService;
        this.meteringService = meteringService;
    }

    @Override
    public void run() {
        super.run();
        String ruleSetName = ValidationRuleSetTpl.RESIDENTIAL_CUSTOMERS.getName();
        String deviceName = getDeviceNamePrefix() + getSerialNumber();
        Optional<ValidationRuleSet> existingRuleSet = validationService.getValidationRuleSet(ruleSetName);
        if (!existingRuleSet.isPresent()){
            throw new UnableToCreate("Unable to find validation ruleset with name " + ruleSetName);
        }
        Optional<Meter> meter = meteringService.findMeterByName(deviceName);
        if (!meter.isPresent()){
            throw new UnableToCreate("Unable to find meter with name " + deviceName);
        }
        validationService.activateValidation(meter.get());
		validationService.enableValidationOnStorage(meter.get());
    }
}
