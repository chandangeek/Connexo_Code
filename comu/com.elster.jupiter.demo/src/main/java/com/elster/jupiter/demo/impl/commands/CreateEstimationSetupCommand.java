/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.builders.EstimationRuleEstimateWithSamplesPostBuilder;
import com.elster.jupiter.demo.impl.builders.EstimationRuleValueFillPostBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.EstimationRuleSetTpl;
import com.elster.jupiter.demo.impl.templates.EstimationTaskTpl;
import com.elster.jupiter.estimation.EstimationRuleSet;
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

    private EstimationRuleSet estimationRuleSet;
    private EstimationRuleSet strictEstimationRuleSet;

    @Inject
    public CreateEstimationSetupCommand(DeviceConfigurationService deviceConfigurationService,
                                        DeviceService deviceService,
                                        TimeService timeService) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.timeService = timeService;
    }

    public void run() {
        createEstimationTask();
        createEstimationRuleSets();
        addEstimationToDeviceConfigurations();
        addEstimationToDevices();
    }

    private void createEstimationTask() {
        Builders.from(EstimationTaskTpl.ALL_ELECTRICITY_DEVICES).get();
    }

    private void createEstimationRuleSets() {
        this.estimationRuleSet = Builders.from(EstimationRuleSetTpl.RESIDENTIAL_CUSTOMERS)
                .withPostBuilder(new EstimationRuleEstimateWithSamplesPostBuilder(this.timeService.getAllRelativePeriod(), 10L))
                .withPostBuilder(new EstimationRuleValueFillPostBuilder())
                .get();

        this.strictEstimationRuleSet = Builders.from(EstimationRuleSetTpl.RESIDENTIAL_CUSTOMERS_STRICT)
                .withPostBuilder(new EstimationRuleEstimateWithSamplesPostBuilder(this.timeService.getAllRelativePeriod(), 2L))
                .get();
    }

    private void addEstimationToDeviceConfigurations() {
        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.estimationRuleSet)
                .stream()
                .filter(configuration -> !DeviceConfigurationTpl.PROSUMERS_VALIDATION_STRICT.getName().equals(configuration.getName()))
                .forEach(configuration -> {
                    configuration.addEstimationRuleSet(this.estimationRuleSet);
                    configuration.save();
                });
        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.strictEstimationRuleSet)
                .stream()
                .filter(configuration -> DeviceConfigurationTpl.PROSUMERS_VALIDATION_STRICT.getName().equals(configuration.getName()))
                .forEach(configuration -> {
                    configuration.addEstimationRuleSet(this.strictEstimationRuleSet);
                    configuration.save();
                });
    }

    private void addEstimationToDevices() {
        Condition devicesForActivation = where("name").like(Constants.Device.STANDARD_PREFIX + "*");
        deviceService.findAllDevices(devicesForActivation)
                .stream().map(Device::forEstimation).forEach(DeviceEstimation::activateEstimation);
    }
}
