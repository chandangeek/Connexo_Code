/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.builders.ValidationRuleDetectMissingValuesPostBuilder;
import com.elster.jupiter.demo.impl.builders.ValidationRuleDetectThresholdViolationPostBuilder;
import com.elster.jupiter.demo.impl.builders.ValidationRuleRegisterIncreasePostBuilder;
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
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class CreateValidationSetupCommand extends CommandWithTransaction {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final Clock clock;
    private final MeteringService meteringService;
    private final ValidationService validationService;

    private ValidationRuleSet validationRuleSet;
    private ValidationRuleSet gasValidationRuleSet;
    private ValidationRuleSet waterValidationRuleSet;

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
                .withVersionPostBuilder(new ValidationRuleRegisterIncreasePostBuilder()
                        .withReadingType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                        .withReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                        .withReadingType("0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0")
                        .withReadingType("0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0")
                        .withReadingType("0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0")
                        .withReadingType("0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0"))
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(1200)
                        .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                        .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0"))
                .get();

        this.gasValidationRuleSet = Builders.from(ValidationRuleSetTpl.RESIDENTIAL_GAS)
                .withVersionPostBuilder(new ValidationRuleRegisterIncreasePostBuilder()
                        .withReadingType("0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(3600)
                        .withReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .get();

        this.waterValidationRuleSet = Builders.from(ValidationRuleSetTpl.RESIDENTIAL_WATER)
                .withVersionPostBuilder(new ValidationRuleRegisterIncreasePostBuilder()
                        .withReadingType("0.0.0.1.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(3600)
                        .withReadingType("0.0.7.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .get();
    }

    private void addValidationToDeviceConfigurations() {
        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.validationRuleSet)
                .stream()
                .forEach(configuration -> {
                    configuration.addValidationRuleSet(this.validationRuleSet);
                    configuration.save();
                });

        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.waterValidationRuleSet)
                .stream()
                .forEach(configuration -> {
                    configuration.addValidationRuleSet(this.waterValidationRuleSet);
                    configuration.save();
                });

        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.gasValidationRuleSet)
                .stream()
                .forEach(configuration -> {
                    configuration.addValidationRuleSet(this.gasValidationRuleSet);
                    configuration.save();
                });
    }


    private void addValidationToDevices() {
        Subquery devicesWithValidateOnStore = this.deviceService.deviceQuery()
                .asSubquery(where("name").like(Constants.Device.STANDARD_PREFIX + "*")
                        .or(where("name").like(Constants.Device.GAS_PREFIX + "*"))
                        .or(where("name").like(Constants.Device.WATER_PREFIX + "*")), "id");
        List<Meter> meters = this.meteringService.getMeterQuery()
                .select(ListOperator.IN.contains(devicesWithValidateOnStore, "amrId"));
        System.out.println("==> Validate on store will be activated for " + meters.size() + " devices");
        meters.forEach(this.validationService::activateValidation);
    }
}
