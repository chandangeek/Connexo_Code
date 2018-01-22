/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.builders.ValidationRuleDetectMissingValuesPostBuilder;
import com.elster.jupiter.demo.impl.builders.ValidationRuleDetectThresholdViolationPostBuilder;
import com.elster.jupiter.demo.impl.builders.ValidationRuleRegisterIncreasePostBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceDataValidationTaskTpl;
import com.elster.jupiter.demo.impl.templates.UsagePointDataValidationTaskTpl;
import com.elster.jupiter.demo.impl.templates.ValidationRuleSetTpl;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.demo.impl.commands.CreateMetrologyConfigurationsCommand.OOTBMetrologyConfiguration.RESIDENTIAL_CONSUMER_WITH_1_METER;
import static com.elster.jupiter.demo.impl.commands.CreateMetrologyConfigurationsCommand.OOTBMetrologyConfiguration.RESIDENTIAL_PROSUMER_WITH_1_METER;
import static com.elster.jupiter.demo.impl.commands.CreateMetrologyConfigurationsCommand.OOTBMetrologyConfiguration.RESIDENTIAL_GAS;
import static com.elster.jupiter.demo.impl.commands.CreateMetrologyConfigurationsCommand.OOTBMetrologyConfiguration.RESIDENTIAL_WATER;
import static com.elster.jupiter.util.conditions.Where.where;

public class CreateValidationSetupCommand extends CommandWithTransaction {

    private final DeviceConfigurationService deviceConfigurationService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final DeviceService deviceService;
    private final MeteringService meteringService;
    private final ValidationService validationService;
    private final LicenseService licenseService;

    private ValidationRuleSet validationRuleSet;
    private ValidationRuleSet gasValidationRuleSet;
    private ValidationRuleSet waterValidationRuleSet;

    @Inject
    public CreateValidationSetupCommand(
            DeviceConfigurationService deviceConfigurationService,
            MetrologyConfigurationService metrologyConfigurationService, UsagePointConfigurationService usagePointConfigurationService, DeviceService deviceService,
            MeteringService meteringService,
            ValidationService validationService,
            LicenseService licenseService) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.deviceService = deviceService;
        this.meteringService = meteringService;
        this.validationService = validationService;
        this.licenseService = licenseService;
    }

    @Override
    public void run() {
        boolean withInsight = licenseService.getLicenseForApplication("INS").isPresent();
        try {
            createMdcValidationTask();
            if (withInsight) {
                createMdmValidationTasks();
                createMdmValidationRuleSets();
            }
            createValidationRuleSet();
            addValidationToDeviceConfigurations();
            addValidationToDevices();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null){
                cause.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void createMdmValidationRuleSets() {
        ValidationRuleSet mdmValidationRuleSet = Builders.from(ValidationRuleSetTpl.RESIDENTIAL_CUSTOMERS)
                .withQualityCodeSystem(QualityCodeSystem.MDM)
                .withVersionPostBuilder(new ValidationRuleDetectMissingValuesPostBuilder()
                        .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                        .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0"))
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(1200, 800)
                        .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                        .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0")).get();
        ValidationRuleSet mdmGasValidationRuleSet = Builders.from(ValidationRuleSetTpl.RESIDENTIAL_GAS)
                .withQualityCodeSystem(QualityCodeSystem.MDM)
                .withVersionPostBuilder(new ValidationRuleDetectMissingValuesPostBuilder()
                        .withReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(3800, 3400)
                        .withReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0")).get();

        ValidationRuleSet mdmWaterValidationRuleSet = Builders.from(ValidationRuleSetTpl.RESIDENTIAL_WATER)
                .withQualityCodeSystem(QualityCodeSystem.MDM)
                .withVersionPostBuilder(new ValidationRuleDetectMissingValuesPostBuilder()
                        .withReadingType("0.0.7.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(3800, 3400)
                        .withReadingType("0.0.7.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0")).get();

        linkToMetrologyConfigurations(mdmValidationRuleSet, mdmGasValidationRuleSet, mdmWaterValidationRuleSet);
    }

    private void linkToMetrologyConfigurations(ValidationRuleSet mdmValidationRuleSet, ValidationRuleSet mdmGasValidationRuleSet, ValidationRuleSet mdmWaterValidationRuleSet) {
        linkToMetrologyConfiguration(mdmValidationRuleSet, RESIDENTIAL_CONSUMER_WITH_1_METER.getName(), RESIDENTIAL_PROSUMER_WITH_1_METER.getName());
        linkToMetrologyConfiguration(mdmGasValidationRuleSet, RESIDENTIAL_GAS.getName());
        linkToMetrologyConfiguration(mdmWaterValidationRuleSet, RESIDENTIAL_WATER.getName());
    }

    private void linkToMetrologyConfiguration(ValidationRuleSet validationRuleSet, String... names) {
        Arrays.asList(names).forEach(name -> {
            MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(name).orElseThrow(() -> new IllegalStateException("Missing metrology config"));
            metrologyConfiguration.getContracts().stream()
                    .filter(metrologyContract -> !usagePointConfigurationService.getMatchingDeliverablesOnValidationRuleSet(metrologyContract, validationRuleSet).isEmpty())
                    .forEach(metrologyContract -> usagePointConfigurationService.addValidationRuleSet(metrologyContract, validationRuleSet));
        });
    }

    private void createMdcValidationTask() {
        Builders.from(DeviceDataValidationTaskTpl.A1800_DEVICES).get();
    }

    private void createMdmValidationTasks() {
        Builders.from(UsagePointDataValidationTaskTpl.RESIDENTIAL_ELECTRICITY).get();
        Builders.from(UsagePointDataValidationTaskTpl.RESIDENTIAL_GAS).get();
        Builders.from(UsagePointDataValidationTaskTpl.RESIDENTIAL_WATER).get();
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
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(1200, 0)
                        .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                        .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0"))
                .get();

        this.gasValidationRuleSet = Builders.from(ValidationRuleSetTpl.RESIDENTIAL_GAS)
                .withVersionPostBuilder(new ValidationRuleRegisterIncreasePostBuilder()
                        .withReadingType("0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(3600, 0)
                        .withReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .get();

        this.waterValidationRuleSet = Builders.from(ValidationRuleSetTpl.RESIDENTIAL_WATER)
                .withVersionPostBuilder(new ValidationRuleRegisterIncreasePostBuilder()
                        .withReadingType("0.0.0.1.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .withVersionPostBuilder(new ValidationRuleDetectThresholdViolationPostBuilder(3600, 0)
                        .withReadingType("0.0.7.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .get();
    }

    private void addValidationToDeviceConfigurations() {
        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.validationRuleSet)
                .forEach(configuration -> {
                    configuration.addValidationRuleSet(this.validationRuleSet);
                    configuration.save();
                });

        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.waterValidationRuleSet)
                .forEach(configuration -> {
                    configuration.addValidationRuleSet(this.waterValidationRuleSet);
                    configuration.save();
                });

        this.deviceConfigurationService.getLinkableDeviceConfigurations(this.gasValidationRuleSet)
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
