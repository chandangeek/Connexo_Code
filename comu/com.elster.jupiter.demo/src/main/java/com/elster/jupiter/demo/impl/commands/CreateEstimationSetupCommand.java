/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.builders.EstimationRuleEstimateWithSamplesPostBuilder;
import com.elster.jupiter.demo.impl.builders.EstimationRuleValueFillPostBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceEstimationTaskTpl;
import com.elster.jupiter.demo.impl.templates.EstimationRuleSetTpl;
import com.elster.jupiter.demo.impl.templates.UsagePointEstimationTaskTpl;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;

import static com.elster.jupiter.util.conditions.Where.where;

public class CreateEstimationSetupCommand extends CommandWithTransaction {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final TimeService timeService;
    private final LicenseService licenseService;

    private EstimationRuleSet estimationRuleSet;
    private EstimationRuleSet gasEstimationRuleSet;
    private EstimationRuleSet waterEstimationRuleSet;

    @Inject
    public CreateEstimationSetupCommand(DeviceConfigurationService deviceConfigurationService,
                                        DeviceService deviceService,
                                        TimeService timeService,
                                        LicenseService licenseService) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.timeService = timeService;
        this.licenseService = licenseService;
    }

    @Override
    public void run() {
        boolean withInsight = licenseService.getLicenseForApplication("INS").isPresent();
        createMdcEstimationTasks();
        if (withInsight) {
            createMdmEstimationTasks();
        }
        createEstimationRuleSets();
        addEstimationToDeviceConfigurations();
        addEstimationToDevices();
    }

    private void createMdcEstimationTasks() {
        Builders.from(DeviceEstimationTaskTpl.ALL_ELECTRICITY_DEVICES).get();
        Builders.from(DeviceEstimationTaskTpl.GAS_DEVICES).get();
        Builders.from(DeviceEstimationTaskTpl.WATER_DEVICES).get();
    }

    private void createMdmEstimationTasks() {
        Builders.from(UsagePointEstimationTaskTpl.RESIDENTIAL_ELECTRICITY).get();
        Builders.from(UsagePointEstimationTaskTpl.RESIDENTIAL_GAS).get();
        Builders.from(UsagePointEstimationTaskTpl.RESIDENTIAL_WATER).get();
    }

    private void createEstimationRuleSets() {
        this.estimationRuleSet = Builders.from(EstimationRuleSetTpl.RESIDENTIAL_CUSTOMERS)
                .withPostBuilder(new EstimationRuleEstimateWithSamplesPostBuilder(this.timeService.getAllRelativePeriod(), TimeDuration.hours(2))
                        .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                        .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0"))
                .withPostBuilder(new EstimationRuleValueFillPostBuilder(900)
                        .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                        .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0"))
                .get();

        this.gasEstimationRuleSet = Builders.from(EstimationRuleSetTpl.RESIDENTIAL_GAS)
                .withPostBuilder(new EstimationRuleEstimateWithSamplesPostBuilder(this.timeService.getAllRelativePeriod(), TimeDuration.hours(8))
                        .withReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .withPostBuilder(new EstimationRuleValueFillPostBuilder(3600)
                        .withReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .get();

        this.waterEstimationRuleSet = Builders.from(EstimationRuleSetTpl.RESIDENTIAL_WATER)
                .withPostBuilder(new EstimationRuleEstimateWithSamplesPostBuilder(this.timeService.getAllRelativePeriod(), TimeDuration.hours(8))
                        .withReadingType("0.0.7.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .withPostBuilder(new EstimationRuleValueFillPostBuilder(3600)
                        .withReadingType("0.0.7.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .get();
    }

    private void addEstimationToDeviceConfigurations() {
        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.estimationRuleSet)
                .forEach(configuration -> {
                    configuration.addEstimationRuleSet(this.estimationRuleSet);
                    configuration.save();
                });

        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.gasEstimationRuleSet)
                .forEach(configuration -> {
                    configuration.addEstimationRuleSet(this.gasEstimationRuleSet);
                    configuration.save();
                });

        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.waterEstimationRuleSet)
                .forEach(configuration -> {
                    configuration.addEstimationRuleSet(this.waterEstimationRuleSet);
                    configuration.save();
                });
    }

    private void addEstimationToDevices() {
        Condition devicesForActivation = where("name").like(Constants.Device.STANDARD_PREFIX + "*")
                .or(where("name").like(Constants.Device.GAS_PREFIX + "*"))
                .or(where("name").like(Constants.Device.WATER_PREFIX + "*"));
        deviceService.findAllDevices(devicesForActivation)
                .stream().map(Device::forEstimation).forEach(DeviceEstimation::activateEstimation);
    }
}
