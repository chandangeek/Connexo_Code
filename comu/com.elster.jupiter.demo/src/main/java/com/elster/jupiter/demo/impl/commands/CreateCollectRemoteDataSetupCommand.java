package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.FavoriteGroupBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.ChannelsOnDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.WebRTUNTASimultationToolPropertyPostBuilder;
import com.elster.jupiter.demo.impl.builders.device.ConnectionsDevicePostBuilder;
import com.elster.jupiter.demo.impl.builders.device.SecurityPropertiesDevicePostBuilder;
import com.elster.jupiter.demo.impl.templates.ComScheduleTpl;
import com.elster.jupiter.demo.impl.templates.ComServerTpl;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.CreationRuleTpl;
import com.elster.jupiter.demo.impl.templates.DataCollectionKpiTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceGroupTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.InboundComPortPoolTpl;
import com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl;
import com.elster.jupiter.demo.impl.templates.LogBookTypeTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortTpl;
import com.elster.jupiter.demo.impl.templates.RegisterGroupTpl;
import com.elster.jupiter.demo.impl.templates.RegisterTypeTpl;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.engine.config.ComServer;

import java.util.EnumSet;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.Optional;

public class CreateCollectRemoteDataSetupCommand {
    private final LicenseService licenseService;
    private final Provider<CreateAssignmentRulesCommand> createAssignmentRulesCommandProvider;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final Provider<ConnectionsDevicePostBuilder> connectionsDevicePostBuilderProvider;

    private String comServerName;
    private String host;
    private int deviceCounter = 0;

    @Inject
    public CreateCollectRemoteDataSetupCommand(
            LicenseService licenseService,
            Provider<CreateAssignmentRulesCommand> createAssignmentRulesCommandProvider,
            Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider,
            Provider<ConnectionsDevicePostBuilder> connectionsDevicePostBuilderProvider) {
        this.licenseService = licenseService;
        this.createAssignmentRulesCommandProvider = createAssignmentRulesCommandProvider;
        this.connectionMethodsProvider = connectionMethodsProvider;
        this.connectionsDevicePostBuilderProvider = connectionsDevicePostBuilderProvider;
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
        createRegisterTypes();
        createRegisterGroups();
        createLogBookTypes();
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

    private void createLogBookTypes(){
        EnumSet.of(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG).stream().forEach(
                tpl->Builders.from(tpl).get()
        );
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
        DeviceConfiguration configuration = createDeviceConfiguration(deviceType, DeviceConfigurationTpl.DEFAULT);
        for(int i = 0; i < deviceTypeTpl.getDeviceCount(); i++){
            deviceCounter++;
            String serialNumber = "01000001" + String.format("%04d", deviceCounter);
            String mrid = Constants.Device.STANDARD_PREFIX +  serialNumber;
            createDevice(configuration, mrid, serialNumber, deviceTypeTpl);
        }
    }

    private DeviceConfiguration createDeviceConfiguration(DeviceType deviceType, DeviceConfigurationTpl deviceConfigurationTpl) {
        DeviceConfiguration configuration = Builders.from(deviceConfigurationTpl).withDeviceType(deviceType)
                .withPostBuilder(this.connectionMethodsProvider.get().withHost(this.host))
                .withPostBuilder(new ChannelsOnDevConfPostBuilder())
                .get();
        configuration.activate();
        return configuration;
    }

    private void createDevice(DeviceConfiguration configuration, String mrid, String serialNumber, DeviceTypeTpl deviceTypeTpl){
        Builders.from(DeviceBuilder.class)
                .withMrid(mrid)
                .withSerialNumber(serialNumber)
                .withDeviceConfiguration(configuration)
                .withComSchedules(Arrays.asList(Builders.from(ComScheduleTpl.DAILY_READ_ALL).get()))
                .withPostBuilder(this.connectionsDevicePostBuilderProvider.get().withComPortPool(Builders.from(deviceTypeTpl.getPoolTpl()).get()).withHost(this.host))
                .withPostBuilder(new SecurityPropertiesDevicePostBuilder())
                .withPostBuilder(new WebRTUNTASimultationToolPropertyPostBuilder())
                .get();
    }

    private void createDeviceGroups() {
        EndDeviceGroup group = Builders.from(DeviceGroupTpl.NORTH_REGION).get();
        Builders.from(FavoriteGroupBuilder.class).withGroup(group).get();

        group = Builders.from(DeviceGroupTpl.SOUTH_REGION).get();
        Builders.from(FavoriteGroupBuilder.class).withGroup(group).get();

        group = Builders.from(DeviceGroupTpl.ALL_ELECTRICITY_DEVICES).get();
        Builders.from(FavoriteGroupBuilder.class).withGroup(group).get();
    }

    private void createKpi() {
        Builders.from(DataCollectionKpiTpl.NORTH_REGION).get();
        Builders.from(DataCollectionKpiTpl.SOUTH_REGION).get();
    }
}