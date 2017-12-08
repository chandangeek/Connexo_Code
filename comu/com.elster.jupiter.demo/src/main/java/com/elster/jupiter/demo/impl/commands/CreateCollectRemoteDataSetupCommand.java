/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.FavoriteDeviceGroupBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.ChannelsOnDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.type.AttachDeviceTypeCPSPostBuilder;
import com.elster.jupiter.demo.impl.commands.devices.CreateHANDeviceCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateSPEDeviceCommand;
import com.elster.jupiter.demo.impl.templates.*;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.ports.ComPortType;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

public class CreateCollectRemoteDataSetupCommand extends CommandWithTransaction {
    public static final int VALIDATION_STRICT_DEVICE_COUNT = 21;
    private final LicenseService licenseService;
    private final DeviceService deviceService;
    private final MeteringService meteringService;
    private final Provider<CreateAssignmentRulesCommand> createAssignmentRulesCommandProvider;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final Provider<AttachDeviceTypeCPSPostBuilder> attachDeviceTypeCPSPostBuilderProvider;
    private final Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider;
    private final Provider<CreateUsagePointsForDevicesCommand> createUsagePointsForDevicesCommandProvider;
    private final Provider<CreateSPEDeviceCommand> createSPEDeviceCommandProvider;
    private final Provider<CreateHANDeviceCommand> createHANDeviceCommandProvider;
    private final Provider<CreateMetrologyConfigurationsCommand> createMetrologyConfigurationsCommandProvider;

    private String comServerName;
    private String host;
    private Integer devicesPerType = null;
    private int deviceCounter = 0;

    @Inject
    public CreateCollectRemoteDataSetupCommand(
            LicenseService licenseService,
            DeviceService deviceService,
            MeteringService meteringService, Provider<CreateAssignmentRulesCommand> createAssignmentRulesCommandProvider,
            Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider,
            Provider<AttachDeviceTypeCPSPostBuilder> attachDeviceTypeCPSPostBuilderProvider,
            Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider,
            Provider<CreateUsagePointsForDevicesCommand> createUsagePointsForDevicesCommandProvider,
            Provider<CreateSPEDeviceCommand> createSPEDeviceCommandProvider,
            Provider<CreateHANDeviceCommand> createHANDeviceCommandProvider, Provider<CreateMetrologyConfigurationsCommand> createMetrologyConfigurationsCommandProvider) {
        this.licenseService = licenseService;
        this.deviceService = deviceService;
        this.meteringService = meteringService;
        this.createAssignmentRulesCommandProvider = createAssignmentRulesCommandProvider;
        this.connectionMethodsProvider = connectionMethodsProvider;
        this.attachDeviceTypeCPSPostBuilderProvider = attachDeviceTypeCPSPostBuilderProvider;
        this.addLocationInfoToDevicesCommandProvider = addLocationInfoToDevicesCommandProvider;
        this.createUsagePointsForDevicesCommandProvider = createUsagePointsForDevicesCommandProvider;
        this.createSPEDeviceCommandProvider = createSPEDeviceCommandProvider;
        this.createHANDeviceCommandProvider = createHANDeviceCommandProvider;
        this.createMetrologyConfigurationsCommandProvider = createMetrologyConfigurationsCommandProvider;
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

    @Override
    public void run() {
        parametersCheck();
        licenseCheck();
        boolean withInsight = licenseService.getLicenseForApplication("INS").isPresent();
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
            createCommandLimitation();
        });
        executeTransaction(() -> {
            if (withInsight) {
                createMetrologyConfigurationsCommandProvider.get().createMetrologyConfigurations();
            } else {
                createMetrologyConfigurationsCommandProvider.get().createMultisenseMetrologyConfigurations();
            }
        });
        createDeviceStructure();
        executeTransaction(() -> {
            createCreationRules();
            createAssignmentRules();
        });
        executeTransaction(this::createDeviceGroups);
        executeTransaction(() -> {
            if (withInsight) {
                createUsagePointGroups();
            }
        });
        executeTransaction(() -> {
            createDataCollectionKpi();
            createDeviceDataQualityKpi();
            if (withInsight) {
                createUsagePointDataQualityKpi();
            }
        });
        executeTransaction(this::addLocationAndUsagePoints);
        executeTransaction(this::corruptDeviceSettingsForIssueManagement);
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
        Builders.from(new InboundComPortPoolTpl(InboundComPortPoolTpl.INBOUND_SERVLET_POOL_NAME, false,
                "com.energyict.mdc.protocol.inbound.dlms.DlmsSerialNumberDiscover", ComPortType.SERVLET)).get();
        Builders.from(new InboundComPortPoolTpl(InboundComPortPoolTpl.INBOUND_SERVLET_BEACON_PSK, true,
                "com.energyict.mdc.protocol.inbound.g3.Beacon3100PushEventNotification", ComPortType.TCP)).get();

        ComServer comServer = Builders.from(ComServerTpl.DEITVS_099).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_1).withComServer(comServer).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_2).withComServer(comServer).get();

        comServer = Builders.from(ComServerTpl.USER_COMSERVER).withName(this.comServerName).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_1).withComServer(comServer).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_2).withComServer(comServer).get();

        Builders.from(OutboundTCPComPortPoolTpl.VODAFONE).get();
        Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get();
        Builders.from(OutboundTCPComPortPoolTpl.OUTBOUND_TCP).get();
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
        Builders.from(ComTaskTpl.BASIC_CHECK).get();
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

    private void createCommandLimitation() {
        Stream.of(CommandRuleTpl.values()).forEach(tpl -> Builders.from(tpl).get());
    }

    private void createMultisenseMetrologyConfigurations() {
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
                .forEach(deviceTypeTpl -> {
                    executeTransaction(() -> createDeviceStructureForDeviceType(deviceTypeTpl));
                    executeTransaction(() -> createDevices(deviceTypeTpl));
                });
    }

    private void createDevices(DeviceTypeTpl deviceTypeTpl) {
        DeviceType deviceType = Builders.from(deviceTypeTpl).get();
        int deviceCount = (this.devicesPerType == null ? deviceTypeTpl.getDeviceCount() : this.devicesPerType);
        if (deviceTypeTpl == DeviceTypeTpl.Elster_A1800 || deviceTypeTpl == DeviceTypeTpl.Elster_AS1440) {
            deviceCount = (int) Math.floor(deviceCount / 2);
        }
        if (deviceTypeTpl == DeviceTypeTpl.Elster_A1800 || deviceTypeTpl == DeviceTypeTpl.Elster_AS1440 || deviceTypeTpl == DeviceTypeTpl.Iskra_38 || deviceTypeTpl == DeviceTypeTpl.Landis_Gyr_ZMD) {
            createDevices(Builders.from(DeviceConfigurationTpl.PROSUMERS).withDeviceType(deviceType).get(), deviceTypeTpl, deviceCount);
        }
        if (deviceTypeTpl == DeviceTypeTpl.Elster_A1800 || deviceTypeTpl == DeviceTypeTpl.Elster_AS1440 || deviceTypeTpl == DeviceTypeTpl.Actaris_SL7000 || deviceTypeTpl == DeviceTypeTpl.Siemens_7ED) {
            createDevices(Builders.from(DeviceConfigurationTpl.CONSUMERS).withDeviceType(deviceType).get(), deviceTypeTpl, deviceCount);
        }
    }

    private void createDevices(DeviceConfiguration configuration, DeviceTypeTpl deviceTypeTpl, int deviceCount) {

        if (deviceCount < 1) {
            deviceCount = 1;
        }
        for (int i = 0; i < deviceCount; i++) {
            this.deviceCounter++;
            String serialNumber = "0100" + String.format("%04d", deviceCounter);
            String devicename = createDevice(configuration, serialNumber, deviceTypeTpl);
            if (deviceTypeTpl == DeviceTypeTpl.Elster_AS1440 || deviceTypeTpl == DeviceTypeTpl.Elster_A1800) {
                DeviceType deviceType = Builders.from(DeviceTypeTpl.BK_GF).get();
                if (deviceType.getConfigurations().isEmpty()) {
                    ChannelsOnDevConfPostBuilder postBuilder = new ChannelsOnDevConfPostBuilder();
                    postBuilder.setOverruledObisCode("0.1.24.2.1.255");
                    Builders.from(DeviceConfigurationTpl.DEFAULT_GAS).withDeviceType(deviceType).withPostBuilder(postBuilder).get().activate();
                }
                createGasDevice(Builders.from(DeviceConfigurationTpl.DEFAULT_GAS).withDeviceType(deviceType).get(), serialNumber, DeviceTypeTpl.BK_GF, devicename);

                deviceType = Builders.from(DeviceTypeTpl.V200PR_6).get();
                if (deviceType.getConfigurations().isEmpty()) {
                    ChannelsOnDevConfPostBuilder postBuilder = new ChannelsOnDevConfPostBuilder();
                    postBuilder.setOverruledObisCode("0.2.24.2.1.255");
                    Builders.from(DeviceConfigurationTpl.DEFAULT_WATER).withDeviceType(deviceType).withPostBuilder(postBuilder).get().activate();
                }
                createWaterDevice(Builders.from(DeviceConfigurationTpl.DEFAULT_GAS).withDeviceType(deviceType).get(), serialNumber, DeviceTypeTpl.V200PR_6, devicename);
            }
        }
    }

    private void createDeviceStructureForDeviceType(DeviceTypeTpl deviceTypeTpl) {
        DeviceType deviceType = Builders.from(deviceTypeTpl).withPostBuilder(this.attachDeviceTypeCPSPostBuilderProvider.get()).get();
        createDeviceConfigurationWithDevices(deviceType, DeviceConfigurationTpl.PROSUMERS, deviceTypeTpl);
        createDeviceConfigurationWithDevices(deviceType, DeviceConfigurationTpl.CONSUMERS, deviceTypeTpl);
    }

    private void createDeviceConfigurationWithDevices(DeviceType deviceType, DeviceConfigurationTpl deviceConfigurationTpl, DeviceTypeTpl deviceTypeTpl) {
        DeviceConfiguration configuration = Builders.from(deviceConfigurationTpl).withDeviceType(deviceType).withValidateOnStore(deviceTypeTpl.isValidateOnStore())
                .withPostBuilder(this.connectionMethodsProvider.get().withHost(host).withDefaultOutboundTcpProperties())
                .withPostBuilder(new ChannelsOnDevConfPostBuilder())
                .get();
        configuration.activate();
    }

    private String createDevice(DeviceConfiguration configuration, String serialNumber, DeviceTypeTpl deviceTypeTpl) {
        CreateSPEDeviceCommand createDeviceCommand = this.createSPEDeviceCommandProvider.get();
        createDeviceCommand.setDeviceTypeTpl(deviceTypeTpl);
        createDeviceCommand.setDeviceConfiguration(configuration);
        createDeviceCommand.setSerialNumber(serialNumber);
        createDeviceCommand.setHost(this.host);
        createDeviceCommand.withLocation();
        return createDeviceCommand.run();
    }

    private void createGasDevice(DeviceConfiguration configuration, String serialNumber, DeviceTypeTpl deviceType, String deviceName) {
        CreateHANDeviceCommand createDeviceCommand = this.createHANDeviceCommandProvider.get();
        createDeviceCommand.setDeviceTypeTpl(deviceType);
        createDeviceCommand.setDeviceConfiguration(configuration);
        createDeviceCommand.setSerialNumber(serialNumber);
        createDeviceCommand.withPrefix(Constants.Device.GAS_PREFIX);
        createDeviceCommand.withComSchedule(ComScheduleTpl.DAILY_READ_ALL_GAS);
        createDeviceCommand.withSerialPrefix(Constants.Device.GAS_WATER_SERIAL_PREFIX);
        createDeviceCommand.withSerialSuffix(Constants.Device.GAS_SERIAL_SUFFIX);
        createDeviceCommand.linkTo(deviceName);
        createDeviceCommand.run();
    }

    private void createWaterDevice(DeviceConfiguration configuration, String serialNumber, DeviceTypeTpl deviceType, String deviceName) {
        CreateHANDeviceCommand createDeviceCommand = this.createHANDeviceCommandProvider.get();
        createDeviceCommand.setDeviceTypeTpl(deviceType);
        createDeviceCommand.setDeviceConfiguration(configuration);
        createDeviceCommand.setSerialNumber(serialNumber);
        createDeviceCommand.withPrefix(Constants.Device.WATER_PREFIX);
        createDeviceCommand.withComSchedule(ComScheduleTpl.DAILY_READ_ALL_WATER);
        createDeviceCommand.withSerialPrefix(Constants.Device.GAS_WATER_SERIAL_PREFIX);
        createDeviceCommand.withSerialSuffix(Constants.Device.WATER_SERIAL_SUFFIX);
        createDeviceCommand.linkTo(deviceName);
        createDeviceCommand.run();
    }

    private void createDeviceGroups() {
        Builders.from(FavoriteDeviceGroupBuilder.class).withGroup(Builders.from(DeviceGroupTpl.NORTH_REGION).get()).get();
        Builders.from(FavoriteDeviceGroupBuilder.class).withGroup(Builders.from(DeviceGroupTpl.SOUTH_REGION).get()).get();
        Builders.from(FavoriteDeviceGroupBuilder.class).withGroup(Builders.from(DeviceGroupTpl.ALL_ELECTRICITY_DEVICES).get()).get();
        Builders.from(FavoriteDeviceGroupBuilder.class).withGroup(Builders.from(DeviceGroupTpl.GAS_DEVICES).get()).get();
        Builders.from(FavoriteDeviceGroupBuilder.class).withGroup(Builders.from(DeviceGroupTpl.WATER_DEVICES).get()).get();
    }

    private void createUsagePointGroups() {
        Builders.from(UsagePointGroupTpl.RESIDENTIAL_ELECTRICITY).get();
        Builders.from(UsagePointGroupTpl.RESIDENTIAL_GAS).get();
        Builders.from(UsagePointGroupTpl.RESIDENTIAL_WATER).get();
    }

    private void createDataCollectionKpi() {
        Builders.from(DataCollectionKpiTpl.NORTH_REGION).get();
        Builders.from(DataCollectionKpiTpl.SOUTH_REGION).get();
    }

    private void createDeviceDataQualityKpi() {
        Builders.from(DeviceDataQualityKpiTpl.NORTH_REGION).get();
        Builders.from(DeviceDataQualityKpiTpl.SOUTH_REGION).get();
    }

    private void createUsagePointDataQualityKpi() {
        Builders.from(UsagePointDataQualityKpiTpl.RESIDENTIAL_ELECTRICITY).get();
        Builders.from(UsagePointDataQualityKpiTpl.RESIDENTIAL_GAS).get();
        Builders.from(UsagePointDataQualityKpiTpl.RESIDENTIAL_WATER).get();
    }

    private void addLocationAndUsagePoints() {
        List<Device> devices = this.deviceService.deviceQuery().select(where("name").like(Constants.Device.STANDARD_PREFIX + "*"));
        this.addLocationInfoToDevicesCommandProvider.get().setDevices(devices).run();
        this.createUsagePointsForDevicesCommandProvider.get().run();
    }

    private void corruptDeviceSettingsForIssueManagement() {
        if(devicesPerType == null) {
            List<Device> devices = deviceService.deviceQuery()
                    .select(where("name").like(Constants.Device.STANDARD_PREFIX + "*")
                            .and(where("deviceConfiguration.name").in(Arrays.asList(DeviceConfigurationTpl.PROSUMERS.getName(), DeviceConfigurationTpl.CONSUMERS.getName()))))
                    .stream()
                    .filter(device -> DefaultState.ACTIVE.getKey().equals(device.getState().getName()))
                    .collect(Collectors.toList());
            int nrOfCorruptedDevices = (int)Math.floor(devices.size() * 0.01);
            Set<String> devicesWithCorruptedConnectionSettings = new HashSet<>();
            for (int i = 0; i < nrOfCorruptedDevices; i++) {
                int devicePosition = (int) ((devices.size() - 1) * Math.random());
                devices.get(devicePosition).getScheduledConnectionTasks().forEach(connectionTask -> {
                    connectionTask.setProperty("host", "UNKNOWN");
                    connectionTask.saveAllProperties();
                });
                devicesWithCorruptedConnectionSettings.add(devices.get(devicePosition).getName());
            }
            System.out.println("==> Devices with corrupted connection settings: " + devicesWithCorruptedConnectionSettings.stream().collect(Collectors.joining(", ")));
        }
    }
}
