package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.DeviceGroupBuilder;
import com.elster.jupiter.demo.impl.builders.FavoriteGroupBuilder;
import com.elster.jupiter.demo.impl.pp.ConnectionMethodForDeviceConfiguration;
import com.elster.jupiter.demo.impl.pp.ProtocolPropertiesMPP;
import com.elster.jupiter.demo.impl.templates.ComScheduleTpl;
import com.elster.jupiter.demo.impl.templates.ComServerTpl;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.CreationRuleTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.InboundComPortPoolTpl;
import com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl;
import com.elster.jupiter.demo.impl.templates.LogBookTypeTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortTpl;
import com.elster.jupiter.demo.impl.templates.ReadingTypeTpl;
import com.elster.jupiter.demo.impl.templates.RegisterGroupTpl;
import com.elster.jupiter.demo.impl.templates.RegisterTypeTpl;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CreateCollectRemoteDataSetupCommand {
    private final LicenseService licenseService;
    private final Provider<CreateAssignmentRulesCommand> createAssignmentRulesCommandProvider;
    private final Provider<ConnectionMethodForDeviceConfiguration> connectionMethodsProvider;
    private ProtocolPluggableService protocolPluggableService;
    private ConnectionTaskService connectionTaskService;

    private String comServerName;
    private String host;
    private int deviceCounter = 0;

    @Inject
    public CreateCollectRemoteDataSetupCommand(
            LicenseService licenseService,
            Provider<CreateAssignmentRulesCommand> createAssignmentRulesCommandProvider,
            Provider<ConnectionMethodForDeviceConfiguration> connectionMethodsProvider,
            ProtocolPluggableService protocolPluggableService,
            ConnectionTaskService connectionTaskService) {
        this.licenseService = licenseService;
        this.createAssignmentRulesCommandProvider = createAssignmentRulesCommandProvider;
        this.connectionMethodsProvider = connectionMethodsProvider;
        this.protocolPluggableService = protocolPluggableService;
        this.connectionTaskService = connectionTaskService;
    }

    public void setComServerName(String comServerName) {
        this.comServerName = comServerName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void run(){
        paramersCheck();
        licenseCheck();
        createComBackground();
        createReadingTypes();
        createRegisterTypes();
        createRegisterGroups();
        createLoogBookTypes();
        createLoadProfileTypes();
        createComTasks();
        createComSchedules();
        createCreationRules();
        createAssignmentRules();
        createDeviceStructure();
        createDeviceGroups();
        createKpi();
    }

    private void paramersCheck() {
        if (this.comServerName == null){
            throw new UnableToCreate("You must specify a name for active com server");
        }
        if (this.host == null){
            throw new UnableToCreate("You must specify a target NTA host");
        }
    }

    private void licenseCheck(){
        Optional<License> license = licenseService.getLicenseForApplication("MDC");
        if (!license.isPresent() || !License.Status.ACTIVE.equals(license.get().getStatus())) {
            throw new IllegalStateException("MDC License isn't installed correctly");
        }
    }

    private void createComBackground(){
        Builders.from(InboundComPortPoolTpl.INBOUND_SERVLET_POOL).get();

        ComServer comServer = Builders.from(ComServerTpl.DEITVS_099).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_1).withComServer(comServer).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_2).withComServer(comServer).get();

        comServer = Builders.from(ComServerTpl.USER_COMSERVER).withName(this.comServerName).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_1).withComServer(comServer).get();
        Builders.from(OutboundTCPComPortTpl.OUTBOUND_TCP_2).withComServer(comServer).get();

        Builders.from(OutboundTCPComPortPoolTpl.VODAFONE).get();
        Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get();
    }

    private void createReadingTypes(){
        for (ReadingTypeTpl descr : ReadingTypeTpl.values()) {
            Builders.from(descr).get();
        }
    }

    private void createRegisterTypes(){
        Builders.from(RegisterTypeTpl.B_F_E_S_M_E).get();
        Builders.from(RegisterTypeTpl.B_R_E_S_M_E).get();
        Builders.from(RegisterTypeTpl.S_F_E_S_M_E_T1).get();
        Builders.from(RegisterTypeTpl.S_F_E_S_M_E_T2).get();
        Builders.from(RegisterTypeTpl.S_R_E_S_M_E_T1).get();
        Builders.from(RegisterTypeTpl.S_R_E_S_M_E_T2).get();
    }

    private void createRegisterGroups(){
        Builders.from(RegisterGroupTpl.DEVICE_DATA).get();
        Builders.from(RegisterGroupTpl.TARIFF_1).get();
        Builders.from(RegisterGroupTpl.TARIFF_2).get();
    }

    private void createLoogBookTypes(){
        Builders.from(LogBookTypeTpl.GENERIC).get();
    }

    private void createLoadProfileTypes(){
        Builders.from(LoadProfileTypeTpl._15_MIN_ELECTRICITY).get();
        Builders.from(LoadProfileTypeTpl.DAILY_ELECTRICITY).get();
        Builders.from(LoadProfileTypeTpl.MONTHLY_ELECTRICITY).get();
    }

    private void createComTasks(){
        Builders.from(ComTaskTpl.READ_ALL).get();
        Builders.from(ComTaskTpl.READ_LOAD_PROFILE_DATA).get();
        Builders.from(ComTaskTpl.READ_REGISTER_DATA).get();
        Builders.from(ComTaskTpl.READ_LOG_BOOK_DATA).get();
        Builders.from(ComTaskTpl.TOPOLOGY).get();
    }

    private void createComSchedules(){
        Builders.from(ComScheduleTpl.DAILY_READ_ALL).get();
    }

    private void createCreationRules(){
        for (CreationRuleTpl ruleTpl : CreationRuleTpl.values()) {
            Builders.from(ruleTpl).get();
        }
    }

    private void createAssignmentRules(){
        CreateAssignmentRulesCommand command = this.createAssignmentRulesCommandProvider.get();
        command.run();
    }

    //TODO Everything below should be refactored
    private void createDeviceStructure(){
        createDeviceStructureForDeviceType(DeviceTypeTpl.Actaris_SL7000);
        createDeviceStructureForDeviceType(DeviceTypeTpl.Elster_AS1440);
        createDeviceStructureForDeviceType(DeviceTypeTpl.Elster_AS3000);
        createDeviceStructureForDeviceType(DeviceTypeTpl.Iskra_38);
        createDeviceStructureForDeviceType(DeviceTypeTpl.Landis_Gyr_ZMD);
        createDeviceStructureForDeviceType(DeviceTypeTpl.Siemens_7ED);
    }

    private void createDeviceStructureForDeviceType(DeviceTypeTpl deviceTypeTpl){
        DeviceType deviceType = Builders.from(deviceTypeTpl).get();
        Builders.from(DeviceConfigurationTpl.EXTENDED).withDeviceType(deviceType)
                .withPropertyProviders(Arrays.asList(
                        this.connectionMethodsProvider.get().withHost(this.host),
                        new ProtocolPropertiesMPP()
                )).get().activate();
        DeviceConfiguration configuration = Builders.from(DeviceConfigurationTpl.DEFAULT).withDeviceType(deviceType)
                .withPropertyProviders(Arrays.asList(
                        this.connectionMethodsProvider.get().withHost(this.host),
                        new ProtocolPropertiesMPP()
                )).get();
        addChannelsToDeviceConfiguration(configuration);
        configuration.activate();
        for(int i = 0; i < deviceTypeTpl.getDeviceCount(); i++){
            deviceCounter++;
            String serialNumber = "01000001" + String.format("%04d", deviceCounter);
            String mrid = Constants.Device.STANDARD_PREFIX +  serialNumber;
            createDevice(configuration, mrid, serialNumber, deviceTypeTpl);
        }
    }

    private void addChannelsToDeviceConfiguration(DeviceConfiguration configuration) {
        for (LoadProfileSpec loadProfileSpec : configuration.getLoadProfileSpecs()) {
            List<ChannelType> availableChannelTypes = loadProfileSpec.getLoadProfileType().getChannelTypes();
            for (ChannelType channelType : availableChannelTypes) {
                configuration.createChannelSpec(channelType, channelType.getPhenomenon(), loadProfileSpec).setMultiplier(new BigDecimal(1)).setOverflow(new BigDecimal(9999999999L)).setNbrOfFractionDigits(0).add();
            }
        }
    }

    private void createDevice(DeviceConfiguration configuration, String mrid, String serialNumber, DeviceTypeTpl deviceTypeTpl){
        Device device = Builders.from(DeviceBuilder.class)
                .withMrid(mrid)
                .withSerialNumber(serialNumber)
                .withDeviceConfiguration(configuration)
                .withComSchedules(Arrays.asList(Builders.from(ComScheduleTpl.DAILY_READ_ALL).get()))
                .get();
        addConnectionMethodToDevice(configuration, device, deviceTypeTpl);
        setSecurityPropertiesForDevice(configuration, device);
    }

    private void setSecurityPropertiesForDevice(DeviceConfiguration configuration, Device device) {
        for (SecurityPropertySet securityPropertySet : configuration.getSecurityPropertySets()) {
            TypedProperties typedProperties = TypedProperties.empty();
            typedProperties.setProperty("ClientMacAddress", new BigDecimal(1));
            device.setSecurityProperties(securityPropertySet, typedProperties);
        }
    }

    private void addConnectionMethodToDevice(DeviceConfiguration configuration, Device device, DeviceTypeTpl deviceTypeTpl) {
        PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
        int portNumber = 4059;
        ScheduledConnectionTask deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
                .setComPortPool(Builders.from(deviceTypeTpl.getPoolTpl()).get())
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .setNextExecutionSpecsFrom(null)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                .setProperty("host", this.host)
                .setProperty("portNumber", new BigDecimal(portNumber))
                .setProperty("connectionTimeout", TimeDuration.minutes(1))
                .setSimultaneousConnectionsAllowed(false)
                .add();
        connectionTaskService.setDefaultConnectionTask(deviceConnectionTask);
    }

    private void createDeviceGroups() {
        EndDeviceGroup group = Builders.from(DeviceGroupBuilder.class)
                .withName(Constants.DeviceGroup.NORTH_REGION)
                .withDeviceTypes(DeviceTypeTpl.Elster_AS1440.getName(), DeviceTypeTpl.Landis_Gyr_ZMD.getName(), DeviceTypeTpl.Actaris_SL7000.getName())
                .get();
        Builders.from(FavoriteGroupBuilder.class).withGroup(group).get();

        group = Builders.from(DeviceGroupBuilder.class)
                .withName(Constants.DeviceGroup.SOUTH_REGION)
                .withDeviceTypes(DeviceTypeTpl.Elster_AS3000.getName(), DeviceTypeTpl.Siemens_7ED.getName(), DeviceTypeTpl.Iskra_38.getName())
                .get();
        Builders.from(FavoriteGroupBuilder.class).withGroup(group).get();

        group = Builders.from(DeviceGroupBuilder.class)
                .withName(Constants.DeviceGroup.ALL_ELECTRICITY_DEVICES)
                .withDeviceTypes(DeviceTypeTpl.Elster_AS1440.getName(), DeviceTypeTpl.Landis_Gyr_ZMD.getName(), DeviceTypeTpl.Actaris_SL7000.getName(),
                        DeviceTypeTpl.Elster_AS3000.getName(), DeviceTypeTpl.Siemens_7ED.getName(), DeviceTypeTpl.Iskra_38.getName())
                .get();
        Builders.from(FavoriteGroupBuilder.class).withGroup(group).get();
    }

    private void createKpi() {
//        Builders.from(DynamicKpiBuilder.class).withGroup(store.get(EndDeviceGroup.class, gr -> gr.getName().equals(Constants.DeviceGroup.NORTH_REGION))).get();
//        Builders.from(DynamicKpiBuilder.class).withGroup(store.get(EndDeviceGroup.class, gr -> gr.getName().equals(Constants.DeviceGroup.SOUTH_REGION))).get();
    }
}