/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.FavoriteGroupBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.ChannelsOnDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.type.AttachDeviceTypeCPSPostBuilder;
import com.elster.jupiter.demo.impl.commands.devices.CreateSPEDeviceCommand;
import com.elster.jupiter.demo.impl.templates.CalendarTpl;
import com.elster.jupiter.demo.impl.templates.ComScheduleTpl;
import com.elster.jupiter.demo.impl.templates.ComServerTpl;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.CreationRuleTpl;
import com.elster.jupiter.demo.impl.templates.DataCollectionKpiTpl;
import com.elster.jupiter.demo.impl.templates.DataValidationKpiTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceGroupTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.InboundComPortPoolTpl;
import com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl;
import com.elster.jupiter.demo.impl.templates.LogBookTypeTpl;
import com.elster.jupiter.demo.impl.templates.MetrologyConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortTpl;
import com.elster.jupiter.demo.impl.templates.RegisterGroupTpl;
import com.elster.jupiter.demo.impl.templates.RegisterTypeTpl;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

public class CreateCollectRemoteDataSetupCommand extends CommandWithTransaction {
    public static final int VALIDATION_STRICT_DEVICE_COUNT = 21;
    private final LicenseService licenseService;
    private final DeviceService deviceService;
    private final Provider<CreateAssignmentRulesCommand> createAssignmentRulesCommandProvider;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final Provider<AttachDeviceTypeCPSPostBuilder> attachDeviceTypeCPSPostBuilderProvider;
    private final Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider;
    private final Provider<CreateUsagePointsForDevicesCommand> createUsagePointsForDevicesCommandProvider;
    private final Provider<CreateSPEDeviceCommand> createSPEDeviceCommandProvider;
    private final Provider<ActivateDevicesCommand> activateDevicesCommandProvider;

    private String comServerName;
    private String host;
    private Integer devicesPerType = null;
    private int deviceCounter = 0;

    @Inject
    public CreateCollectRemoteDataSetupCommand(
            LicenseService licenseService,
            DeviceService deviceService,
            Provider<CreateAssignmentRulesCommand> createAssignmentRulesCommandProvider,
            Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider,
            Provider<AttachDeviceTypeCPSPostBuilder> attachDeviceTypeCPSPostBuilderProvider,
            Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider,
            Provider<CreateUsagePointsForDevicesCommand> createUsagePointsForDevicesCommandProvider,
            Provider<CreateSPEDeviceCommand> createSPEDeviceCommandProvider,
            Provider<ActivateDevicesCommand> activateDevicesCommandProvider) {
        this.licenseService = licenseService;
        this.deviceService = deviceService;
        this.createAssignmentRulesCommandProvider = createAssignmentRulesCommandProvider;
        this.connectionMethodsProvider = connectionMethodsProvider;
        this.attachDeviceTypeCPSPostBuilderProvider = attachDeviceTypeCPSPostBuilderProvider;
        this.addLocationInfoToDevicesCommandProvider = addLocationInfoToDevicesCommandProvider;
        this.createUsagePointsForDevicesCommandProvider = createUsagePointsForDevicesCommandProvider;
        this.createSPEDeviceCommandProvider = createSPEDeviceCommandProvider;
        this.activateDevicesCommandProvider = activateDevicesCommandProvider;
    }

    public void setComServerName(String comServerName) {
        this.comServerName = comServerName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setDevicesPerType(Integer devicesPerType) {
        this.devicesPerType = devicesPerType;
    }

    public void run() {
        parametersCheck();
        licenseCheck();
        executeTransaction(() -> {
            createComBackground();
            createRegisterTypes();
            createRegisterGroups();
            createLogBookTypes();
            createLoadProfileTypes();
        });
        executeTransaction(() -> {
            createComTasks();
            createComSchedules();
            createCalendars();
        });
        executeTransaction(this::createMetrologyConfigurations);
        createDeviceStructure();
        executeTransaction(() -> {
            createCreationRules();
            createAssignmentRules();
        });
        executeTransaction(this::createDeviceGroups);
        executeTransaction(() -> {
            createDataCollectionKpi();
            createDataValidationKpi();
        });
        executeTransaction(() -> {
            processDevices();
            corruptDeviceSettingsForIssueManagement();
        });
    }

    private void parametersCheck() {
        if (this.comServerName == null) {
            throw new UnableToCreate("You must specify a name for active com server");
        }
        if (this.host == null) {
            throw new UnableToCreate("You must specify a target NTA host");
        }
    }

    private void licenseCheck() {
        Optional<License> license = licenseService.getLicenseForApplication("MDC");
        if (!license.isPresent() || !License.Status.ACTIVE.equals(license.get().getStatus())) {
            throw new IllegalStateException("MDC License isn't installed correctly");
        }
    }

    private void createComBackground() {
        Builders.from(new InboundComPortPoolTpl(InboundComPortPoolTpl.INBOUND_SERVLET_POOL_NAME)).get();

        ComServer comServer = Builders.from(ComServerTpl.DEITVS_099).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_1).withComServer(comServer).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_2).withComServer(comServer).get();

        comServer = Builders.from(ComServerTpl.USER_COMSERVER).withName(this.comServerName).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_1).withComServer(comServer).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_2).withComServer(comServer).get();

        Builders.from(OutboundTCPComPortPoolTpl.VODAFONE).get();
        Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get();
    }

    private void createRegisterTypes() {
        Builders.from(RegisterTypeTpl.SECONDARY_BULK_A_PLUS).get();
        Builders.from(RegisterTypeTpl.SECONDARY_BULK_A_MINUS).get();
        Builders.from(RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1).get();
        Builders.from(RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2).get();
        Builders.from(RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1).get();
        Builders.from(RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2).get();
    }

    private void createRegisterGroups() {
        Builders.from(RegisterGroupTpl.DEVICE_DATA).get();
        Builders.from(RegisterGroupTpl.TARIFF_1).get();
        Builders.from(RegisterGroupTpl.TARIFF_2).get();
    }

    private void createLogBookTypes() {
        EnumSet.of(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG)
                .forEach(tpl -> Builders.from(tpl).get());
    }

    private void createLoadProfileTypes() {
        Builders.from(LoadProfileTypeTpl._15_MIN_ELECTRICITY).get();
        Builders.from(LoadProfileTypeTpl.DAILY_ELECTRICITY).get();
        Builders.from(LoadProfileTypeTpl.MONTHLY_ELECTRICITY).get();
        Builders.from(LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS).get();
        Builders.from(LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS).get();
        Builders.from(LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS).get();
    }

    private void createComTasks() {
        Builders.from(ComTaskTpl.READ_LOAD_PROFILE_DATA).get();
        Builders.from(ComTaskTpl.READ_REGISTER_DATA).get();
        Builders.from(ComTaskTpl.READ_LOG_BOOK_DATA).get();
        Builders.from(ComTaskTpl.TOPOLOGY).get();
        Builders.from(ComTaskTpl.COMMANDS).get();
    }

    private void createComSchedules() {
        Builders.from(ComScheduleTpl.DAILY_READ_ALL).get();
    }

    private void createCreationRules() {
        for (CreationRuleTpl ruleTpl : CreationRuleTpl.values()) {
            Builders.from(ruleTpl).get();
        }
    }

    private void createAssignmentRules() {
        CreateAssignmentRulesCommand command = this.createAssignmentRulesCommandProvider.get();
        command.run();
    }

    private void createCalendars() {
        Stream.of(CalendarTpl.values()).forEach(tpl -> Builders.from(tpl).get());
    }

    private void createMetrologyConfigurations() {
        Builders.from(MetrologyConfigurationTpl.CONSUMER).get();
        Builders.from(MetrologyConfigurationTpl.PROSUMER).get();
    }

    private void createDeviceStructure() {
        Stream.of(DeviceTypeTpl.Actaris_SL7000,
                DeviceTypeTpl.Elster_AS1440,
                DeviceTypeTpl.Elster_A1800,
                DeviceTypeTpl.Iskra_38,
                DeviceTypeTpl.Landis_Gyr_ZMD,
                DeviceTypeTpl.Siemens_7ED)
                .forEach(deviceTypeTpl -> {executeTransaction(() -> createDeviceStructureForDeviceType(deviceTypeTpl));
                            executeTransaction(() -> createDevices(deviceTypeTpl));});
    }

    private void createDevices(DeviceTypeTpl deviceTypeTpl){
        DeviceType deviceType = Builders.from(deviceTypeTpl).get();
        int deviceCount = (this.devicesPerType == null ? deviceTypeTpl.getDeviceCount() : this.devicesPerType);
        if (deviceTypeTpl == DeviceTypeTpl.Elster_A1800) {
            int validationStrictDeviceCount = this.devicesPerType == null ? VALIDATION_STRICT_DEVICE_COUNT : this.devicesPerType / 3; // 3 device conf on this type
            createDevices(Builders.from(DeviceConfigurationTpl.PROSUMERS_VALIDATION_STRICT).withDeviceType(deviceType).get(), deviceTypeTpl,  validationStrictDeviceCount);
            deviceCount = Math.max(0, deviceCount - validationStrictDeviceCount);
        }
        createDevices(Builders.from(DeviceConfigurationTpl.PROSUMERS).withDeviceType(deviceType).get(), deviceTypeTpl, deviceCount >> 1);
        createDevices(Builders.from(DeviceConfigurationTpl.CONSUMERS).withDeviceType(deviceType).get(), deviceTypeTpl, deviceCount >> 1);
    }

    private void createDevices(DeviceConfiguration configuration, DeviceTypeTpl deviceTypeTpl, int deviceCount){

        if (deviceCount < 1) {
            deviceCount = 1;
        }
        for (int i = 0; i < deviceCount; i++) {
            this.deviceCounter++;
            String serialNumber = "01000001" + String.format("%04d", deviceCounter);
            createDevice(configuration, serialNumber, deviceTypeTpl);
        }
    }

    private void createDeviceStructureForDeviceType(DeviceTypeTpl deviceTypeTpl) {
        DeviceType deviceType = Builders.from(deviceTypeTpl).withPostBuilder(this.attachDeviceTypeCPSPostBuilderProvider.get()).get();
        if (deviceTypeTpl == DeviceTypeTpl.Elster_A1800) {
            createDeviceConfigurationWithDevices(deviceType, DeviceConfigurationTpl.PROSUMERS_VALIDATION_STRICT);
        }
        createDeviceConfigurationWithDevices(deviceType, DeviceConfigurationTpl.PROSUMERS);
        createDeviceConfigurationWithDevices(deviceType, DeviceConfigurationTpl.CONSUMERS);
    }

    private void createDeviceConfigurationWithDevices(DeviceType deviceType, DeviceConfigurationTpl deviceConfigurationTpl) {
        DeviceConfiguration configuration = Builders.from(deviceConfigurationTpl).withDeviceType(deviceType)
                .withPostBuilder(this.connectionMethodsProvider.get().withHost(host).withDefaultOutboundTcpProperties())
                .withPostBuilder(new ChannelsOnDevConfPostBuilder())
                .get();
        configuration.activate();
    }

    private void createDevice(DeviceConfiguration configuration, String serialNumber, DeviceTypeTpl deviceTypeTpl) {
        CreateSPEDeviceCommand createDeviceCommand = this.createSPEDeviceCommandProvider.get();
        createDeviceCommand.setDeviceTypeTpl(deviceTypeTpl);
        createDeviceCommand.setDeviceConfiguration(configuration);
        createDeviceCommand.setSerialNumber(serialNumber);
        createDeviceCommand.setHost(this.host);
        createDeviceCommand.run();
    }

    private void createDeviceGroups() {
        Builders.from(FavoriteGroupBuilder.class).withGroup(Builders.from(DeviceGroupTpl.NORTH_REGION).get()).get();
        Builders.from(FavoriteGroupBuilder.class).withGroup(Builders.from(DeviceGroupTpl.SOUTH_REGION).get()).get();
        Builders.from(FavoriteGroupBuilder.class).withGroup(Builders.from(DeviceGroupTpl.ALL_ELECTRICITY_DEVICES).get()).get();
    }

    private void createDataCollectionKpi() {
        Builders.from(DataCollectionKpiTpl.NORTH_REGION).get();
        Builders.from(DataCollectionKpiTpl.SOUTH_REGION).get();
    }

    private void createDataValidationKpi() {
        Builders.from(DataValidationKpiTpl.ALL_ELECTRICITY_DEVICES).get();
    }

    private void processDevices() {
        List<Device> devices = this.deviceService.deviceQuery().select(where("name").like(Constants.Device.STANDARD_PREFIX + "*"));
        int deviceCount = devices.size();
        Predicate<Device> skipActivationFilter = device -> { // skip 5% of devices
            if (deviceCount <= 20) {
                return device.getId() % deviceCount != 0;
            }
            return device.getId() % (deviceCount / (1 + (int) (deviceCount * 0.05))) != 0;
        };
        this.activateDevicesCommandProvider.get().setDevices(devices).setDeviceTransitionFilter(skipActivationFilter).run();
        devices = this.deviceService.deviceQuery().select(where("name").like(Constants.Device.STANDARD_PREFIX + "*"));
        this.addLocationInfoToDevicesCommandProvider.get().setDevices(devices).run();
        this.createUsagePointsForDevicesCommandProvider.get().setDevices(devices).run();
    }

    private void corruptDeviceSettingsForIssueManagement() {
        List<Device> devices = deviceService.deviceQuery()
                .select(where("name").like(Constants.Device.STANDARD_PREFIX + "*")
                        .and(where("deviceConfiguration.name").in(Arrays.asList(DeviceConfigurationTpl.PROSUMERS.getName(), DeviceConfigurationTpl.CONSUMERS.getName()))));
        Set<String> devicesWithCorruptedConnectionSettings = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            int devicePosition = (int) ((devices.size() - 1) * Math.random());
            devices.get(devicePosition).getScheduledConnectionTasks().forEach(connectionTask -> {
                connectionTask.setProperty(ConnectionTypePropertySpecName.OUTBOUND_IP_HOST.propertySpecName(), "UNKNOWN");
                connectionTask.saveAllProperties();
            });
            devicesWithCorruptedConnectionSettings.add(devices.get(devicePosition).getName());
        }
        System.out.println("==> Devices with corrupted connection settings: " + devicesWithCorruptedConnectionSettings.stream().collect(Collectors.joining(", ")));
    }
}
