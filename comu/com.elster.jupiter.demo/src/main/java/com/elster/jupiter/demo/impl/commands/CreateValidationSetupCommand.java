package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.commands.devices.CreateValidationDeviceCommand;
import com.elster.jupiter.demo.impl.templates.ValidationRuleSetTpl;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class CreateValidationSetupCommand {
    public static final String MOCKED_VALIDATION_DEVICE_MRID_PREFIX = Constants.Device.MOCKED_VALIDATION_DEVICE;

    private final ValidationService validationService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringService meteringService;
    private final Provider<CreateValidationDeviceCommand> createValidationDeviceCommandProvider;

    private ValidationRuleSet validationRuleSet;

    @Inject
    public CreateValidationSetupCommand(
            ValidationService validationService,
            DeviceConfigurationService deviceConfigurationService,
            MeteringService meteringService,
            Provider<CreateValidationDeviceCommand> createValidationDeviceCommandProvider) {
        this.validationService = validationService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
        this.createValidationDeviceCommandProvider = createValidationDeviceCommandProvider;
    }

    public void run(){
        createValidationRuleSet();
        createValidationDevice();
        addValidationToDeviceConfigurations();
        addValidationToDevices();
    }

    private void createValidationRuleSet(){
        this.validationRuleSet = Builders.from(ValidationRuleSetTpl.RESIDENTIAL_CUSTOMERS).get();
    }

    private void createValidationDevice(){
        CreateValidationDeviceCommand command = this.createValidationDeviceCommandProvider.get();
        command.setMridPrefix(MOCKED_VALIDATION_DEVICE_MRID_PREFIX);
        command.setSerialNumber("085600010352"); // TODO
        command.run();
    }

    private void addValidationToDeviceConfigurations(){
        List<DeviceConfiguration> configurations = deviceConfigurationService.getLinkableDeviceConfigurations(this.validationRuleSet);
        for (DeviceConfiguration configuration : configurations) {
            System.out.println("==> Validation rule set added to: " + configuration.getName() + " (id = " + configuration.getId() + ")");
            configuration.addValidationRuleSet(this.validationRuleSet);
            configuration.save();
        }
    }

    private void addValidationToDevices(){
        Condition devicesForActivation = where("mRID").like(MOCKED_VALIDATION_DEVICE_MRID_PREFIX + "*").or(where("mRID").like(Constants.Device.STANDARD_PREFIX + "*"));
        List<Meter> meters = meteringService.getMeterQuery().select(devicesForActivation);
        System.out.println("==> Validation will be activated for " + meters.size() + " devices");
        for (Meter meter : meters) {
            validationService.activateValidation(meter, true);
        }
    }
}
