/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.builders.Builder;
import com.elster.jupiter.demo.impl.builders.DeviceConfigurationBuilder;
import com.elster.jupiter.demo.impl.builders.device.SetCustomAttributeValuesToDevicePostBuilder;
import com.elster.jupiter.demo.impl.builders.type.AttachDeviceTypeCPSPostBuilder;
import com.elster.jupiter.demo.impl.templates.CalendarTpl;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.CreationRuleTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceGroupTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl;
import com.elster.jupiter.demo.impl.templates.ValidationRuleSetTpl;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.ValidationVersionStatus;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.calendars.ProtocolSupportedCalendarOptions;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

public class DemoDataUpgrade10_1_Command {

    private static final List<DeviceTypeTpl> SPE_DEVICE_TYPES = Arrays.asList(DeviceTypeTpl.Elster_AS1440, DeviceTypeTpl.Elster_A1800,
            DeviceTypeTpl.Landis_Gyr_ZMD, DeviceTypeTpl.Actaris_SL7000, DeviceTypeTpl.Siemens_7ED, DeviceTypeTpl.Iskra_38);

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final ValidationService validationService;
    private final IssueAssignmentService issueAssignmentService;
    private final IssueCreationService issueCreationService;
    private final Clock clock;
    private final Provider<AttachDeviceTypeCPSPostBuilder> attachDeviceTypeCPSPostBuilderProvider;
    private final Provider<SetCustomAttributeValuesToDevicePostBuilder> setCustomAttributeValuesToDevicePostBuilderProvider;
    private final Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider;

    @Inject
    public DemoDataUpgrade10_1_Command(DeviceConfigurationService deviceConfigurationService,
                                       DeviceService deviceService,
                                       ValidationService validationService,
                                       IssueAssignmentService issueAssignmentService,
                                       IssueCreationService issueCreationService,
                                       Clock clock,
                                       Provider<AttachDeviceTypeCPSPostBuilder> attachDeviceTypeCPSPostBuilderProvider,
                                       Provider<SetCustomAttributeValuesToDevicePostBuilder> setCustomAttributeValuesToDevicePostBuilderProvider,
                                       Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.validationService = validationService;
        this.issueAssignmentService = issueAssignmentService;
        this.issueCreationService = issueCreationService;
        this.clock = clock;
        this.attachDeviceTypeCPSPostBuilderProvider = attachDeviceTypeCPSPostBuilderProvider;
        this.setCustomAttributeValuesToDevicePostBuilderProvider = setCustomAttributeValuesToDevicePostBuilderProvider;
        this.addLocationInfoToDevicesCommandProvider = addLocationInfoToDevicesCommandProvider;
    }

    public void run() {
        SPE_DEVICE_TYPES.forEach(tpl -> {
            DeviceType deviceType = Builders.from(tpl).get();
            addCalendarsToDeviceType(deviceType);
            addLoadProfilesToDeviceType(deviceType);
            this.attachDeviceTypeCPSPostBuilderProvider.get().accept(deviceType);
            deviceType.update();
            deviceType.getConfigurations().stream()
                    .filter(deviceConfiguration -> "Default".equals(deviceConfiguration.getName()))
                    .forEach(deviceConfiguration -> {
                        deviceConfiguration.setName(DeviceConfigurationTpl.PROSUMERS.getName());
                        DeviceConfigurationBuilder.addComTask(deviceConfiguration, Builders.from(ComTaskTpl.COMMANDS).get());
                        deviceConfiguration.save();
                    });
        });
        setDeviceCpsValuesAndUsagePointLocation();
        updateValidationRule();
        updateIssueRules();
    }

    private void addCalendarsToDeviceType(DeviceType deviceType) {
        TimeOfUseOptions timeOfUseOptions = this.deviceConfigurationService.findTimeOfUseOptions(deviceType)
                .orElseGet(() -> this.deviceConfigurationService.newTimeOfUseOptions(deviceType));
        Set<ProtocolSupportedCalendarOptions> deviceTypeTimeOfUseOptions = new HashSet<>(Arrays.asList(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR,
                ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME));
        deviceTypeTimeOfUseOptions.retainAll(this.deviceConfigurationService.getSupportedTimeOfUseOptionsFor(deviceType, false));
        timeOfUseOptions.setOptions(deviceTypeTimeOfUseOptions);
        timeOfUseOptions.save();
        Stream.of(CalendarTpl.RE_CU_01, CalendarTpl.RE_CU_02)
                .map(Builders::from)
                .map(Builder::get)
                .forEach(deviceType::addCalendar);
    }

    private void addLoadProfilesToDeviceType(DeviceType deviceType) {
        List<Long> existingLoadProfileTypes = deviceType.getLoadProfileTypes().stream()
                .map(LoadProfileType::getId)
                .collect(Collectors.toList());
        Stream.of(LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS)
                .map(Builders::from)
                .map(Builder::get)
                .filter(lpt -> !existingLoadProfileTypes.contains(lpt.getId()))
                .forEach(deviceType::addLoadProfileType);
    }

    private void setDeviceCpsValuesAndUsagePointLocation() {
        SetCustomAttributeValuesToDevicePostBuilder customAttributeValuesToDevicePostBuilder = this.setCustomAttributeValuesToDevicePostBuilderProvider.get();
        AddLocationInfoToDevicesCommand addLocationInfoToDevicesCommand = this.addLocationInfoToDevicesCommandProvider.get();
        this.deviceService.deviceQuery().select(where("name").like(Constants.Device.STANDARD_PREFIX + "*"))
                .forEach(device -> {
                    customAttributeValuesToDevicePostBuilder.accept(device);
                    addLocationInfoToDevicesCommand.setDevices(Collections.singletonList(device)).run();
                    device.getUsagePoint().ifPresent(usagePoint -> {
                        device.getLocation().map(Location::getId).ifPresent(usagePoint::setLocation);
                        device.getSpatialCoordinates().ifPresent(usagePoint::setSpatialCoordinates);
                        usagePoint.update();
                    });
                });
    }

    private void updateValidationRule() {
        this.validationService.getValidationRuleSet(ValidationRuleSetTpl.RESIDENTIAL_CUSTOMERS.getName())
                .ifPresent(validationRuleSet -> validationRuleSet.getRuleSetVersions()
                        .stream()
                        .filter(version -> ValidationVersionStatus.CURRENT.equals(version.getStatus()))
                        .findFirst()
                        .ifPresent(version -> version.getRules().stream()
                                .filter(rule -> "com.elster.jupiter.validators.impl.ThresholdValidator".equals(rule.getImplementation()))
                                .findFirst()
                                .ifPresent(rule -> {
                                    Map<String, Object> properties = rule.getProperties().stream()
                                            .collect(Collectors.toMap(ValidationRuleProperties::getName, ValidationRuleProperties::getValue));
                                    properties.put("maximum", new BigDecimal(1200L));
                                    version.updateRule(rule.getId(), rule.getName(), rule.isActive(), rule.getAction(),
                                            rule.getReadingTypes().stream().map(ReadingType::getMRID).collect(Collectors.toList()), properties);
                                    version.save();
                                })));
        this.validationService.findValidationTaskByName(DeviceGroupTpl.A1800_DEVICES.getName())
                .filter(task -> task.getNextExecution() == null)
                .ifPresent(task -> {
                    task.setNextExecution(this.clock.instant());
                    task.setName(task.getName());
                    task.update();
                });
    }

    private void updateIssueRules() {
        this.issueAssignmentService.getAssignmentRuleQuery().select(where("description").isEqualTo("Assign all issues to Monica (default)"))
                .forEach(AssignmentRule::delete);
        this.issueCreationService.getCreationRuleQuery().select(where("name").isEqualTo(CreationRuleTpl.CANNOT_ESTIMATE_SUSPTECTS.getName()))
                .forEach(CreationRule::delete);
    }
}
