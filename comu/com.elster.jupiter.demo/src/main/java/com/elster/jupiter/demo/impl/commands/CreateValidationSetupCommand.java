package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.builders.ValidationRuleDetectMissingValuesPostBuilder;
import com.elster.jupiter.demo.impl.builders.ValidationRuleDetectThresholdViolationPostBuilder;
import com.elster.jupiter.demo.impl.builders.ValidationRuleRegisterIncreasePostBuilder;
import com.elster.jupiter.demo.impl.commands.devices.CreateValidationDeviceCommand;
import com.elster.jupiter.demo.impl.templates.DataValidationTaskTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.ValidationRuleSetTpl;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class CreateValidationSetupCommand {
    public static final String MOCKED_VALIDATION_DEVICE_MRID_PREFIX = Constants.Device.MOCKED_VALIDATION_DEVICE;

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final Clock clock;
    private final Provider<CreateValidationDeviceCommand> createValidationDeviceCommandProvider;

    private ValidationRuleSet validationRuleSet;
    private ValidationRuleSet strictValidationRuleSet;

    @Inject
    public CreateValidationSetupCommand(
            DeviceConfigurationService deviceConfigurationService,
            DeviceService deviceService,
            Clock clock,
            Provider<CreateValidationDeviceCommand> createValidationDeviceCommandProvider) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.clock = clock;
        this.deviceService = deviceService;
        this.createValidationDeviceCommandProvider = createValidationDeviceCommandProvider;
    }

    public void run() {
        createValidationTask();
        createValidationRuleSet();
        addValidationToDeviceConfigurations();
        addValidationToDevices();
    }

    private void createValidationTask() {
        Builders.from(DataValidationTaskTpl.A1800_DEVICES).get();
    }

    private void createValidationRuleSet() {
        this.validationRuleSet = Builders.from(ValidationRuleSetTpl.RESIDENTIAL_CUSTOMERS)
                .withVersionPostBuilder(new ValidationRuleRegisterIncreasePostBuilder())
                .withVersionPostBuilder(new ValidationRuleDetectMissingValuesPostBuilder())
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(1200))
                .get();
        this.strictValidationRuleSet = Builders.from(ValidationRuleSetTpl.RESIDENTIAL_CUSTOMERS_STRICT)
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(900))
                .get();
    }

    private void createValidationDevice() {
        CreateValidationDeviceCommand command = this.createValidationDeviceCommandProvider.get();
        command.setMridPrefix(MOCKED_VALIDATION_DEVICE_MRID_PREFIX);
        command.setSerialNumber("085600010352"); // TODO
        command.run();
    }

    private void addValidationToDeviceConfigurations() {
        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.validationRuleSet)
                .stream()
                .forEach(configuration -> {
                    if (DeviceConfigurationTpl.PROSUMERS_VALIDATION_STRICT.getName().equals(configuration.getName())) {
                        configuration.addValidationRuleSet(this.strictValidationRuleSet);
                        configuration.save();
                    } else {
                        configuration.addValidationRuleSet(this.validationRuleSet);
                        configuration.save();
                    }
                });
    }

    private void addValidationToDevices() {
        List<Device> devices = deviceService.deviceQuery().select(where("mRID").like(Constants.Device.STANDARD_PREFIX + "*"));
        System.out.println("==> Validation will be activated for " + devices.size() + " devices");
        DeviceType elsterA1800DeviceType = Builders.from(DeviceTypeTpl.Elster_A1800).get();
        Instant now = this.clock.instant();
        devices.forEach(device -> {
            if (device.getDeviceType().equals(elsterA1800DeviceType)) {
                device.forValidation().activateValidation(now);
            } else {
                device.forValidation().activateValidationOnStorage(now);
            }
        });
    }
}
