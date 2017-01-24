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
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
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

public class CreateValidationSetupCommand extends CommandWithTransaction {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final Clock clock;
    private final MeteringService meteringService;
    private final ValidationService validationService;

    private ValidationRuleSet validationRuleSet;
    private ValidationRuleSet strictValidationRuleSet;

    @Inject
    public CreateValidationSetupCommand(
            DeviceConfigurationService deviceConfigurationService,
            DeviceService deviceService,
            Clock clock,
            MeteringService meteringService,
            ValidationService validationService) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.clock = clock;
        this.deviceService = deviceService;
        this.meteringService = meteringService;
        this.validationService = validationService;
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

    private void addValidationToDeviceConfigurations() {
        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.validationRuleSet)
                .stream()
                .filter(configuration -> !DeviceConfigurationTpl.PROSUMERS_VALIDATION_STRICT.getName().equals(configuration.getName()))
                .forEach(configuration -> {
                    configuration.addValidationRuleSet(this.validationRuleSet);
                    configuration.save();
                });
        Builders.from(DeviceTypeTpl.Elster_A1800).get().getConfigurations()
                .stream()
                .filter(configuration -> DeviceConfigurationTpl.PROSUMERS_VALIDATION_STRICT.getName().equals(configuration.getName()))
                .filter(configuration -> configuration.getValidationRuleSets().stream().map(ValidationRuleSet::getId).noneMatch(id -> id == this.strictValidationRuleSet.getId()))
                .forEach(configuration -> {
                    configuration.addValidationRuleSet(this.strictValidationRuleSet);
                    configuration.save();
                });
    }


    private void addValidationToDevices() {
        DeviceType elsterA1800DeviceType = Builders.from(DeviceTypeTpl.Elster_A1800).get();
        Subquery devicesWithValidateOnStore = this.deviceService.deviceQuery()
                .asSubquery(where("name").like(Constants.Device.STANDARD_PREFIX + "*").and(where("deviceType").isNotEqual(elsterA1800DeviceType)), "id");
        List<Meter> meters = this.meteringService.getMeterQuery()
                .select(ListOperator.IN.contains(devicesWithValidateOnStore, "amrId"));
        System.out.println("==> Validate on store will be activated for " + meters.size() + " devices");
        meters.forEach(meter -> {
            this.validationService.activateValidation(meter);
            this.validationService.enableValidationOnStorage(meter);
        });

        devicesWithValidateOnStore = this.deviceService.deviceQuery()
                .asSubquery(where("name").like(Constants.Device.STANDARD_PREFIX + "*").and(where("deviceType").isEqualTo(elsterA1800DeviceType)), "id");
        meters = this.meteringService.getMeterQuery()
                .select(ListOperator.IN.contains(devicesWithValidateOnStore, "amrId"));
        System.out.println("==> Validation (without validate on store) will be activated for " + meters.size() + " devices");
        meters.forEach(meter -> {
            this.validationService.activateValidation(meter);
            this.validationService.disableValidationOnStorage(meter);
        });
    }
}
