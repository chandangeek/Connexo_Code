package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DeviceConfigurationBuilder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.GatewayType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum DeviceConfigurationTpl implements Template<DeviceConfiguration, DeviceConfigurationBuilder> {
    DEFAULT("Default", GatewayType.HOME_AREA_NETWORK,
        Arrays.<SecurityPropertySetTpl>asList(SecurityPropertySetTpl.NO_SECURITY, SecurityPropertySetTpl.HIGH_LEVEL),
        Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E, RegisterTypeTpl.S_F_E_S_M_E_T1, RegisterTypeTpl.S_F_E_S_M_E_T2, RegisterTypeTpl.S_R_E_S_M_E_T1, RegisterTypeTpl.S_R_E_S_M_E_T2),
        Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
        Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG),
        Arrays.asList(ComTaskTpl.excludeTopologyTpls())),
    EXTENDED("Extended", GatewayType.HOME_AREA_NETWORK,
        Arrays.<SecurityPropertySetTpl>asList(SecurityPropertySetTpl.NO_SECURITY, SecurityPropertySetTpl.HIGH_LEVEL),
        Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E, RegisterTypeTpl.S_F_E_S_M_E_T1, RegisterTypeTpl.S_F_E_S_M_E_T2, RegisterTypeTpl.S_R_E_S_M_E_T1, RegisterTypeTpl.S_R_E_S_M_E_T2),
        Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
        Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG),
        Arrays.asList(ComTaskTpl.excludeTopologyTpls())),
    AM540("Default", GatewayType.HOME_AREA_NETWORK,
        Collections.<SecurityPropertySetTpl>emptyList(),
        Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E, RegisterTypeTpl.S_F_E_S_M_E_T1, RegisterTypeTpl.S_F_E_S_M_E_T2, RegisterTypeTpl.S_R_E_S_M_E_T1, RegisterTypeTpl.S_R_E_S_M_E_T2),
        Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
        Collections.singletonList(LogBookTypeTpl.GENERIC),
        Arrays.<ComTaskTpl>asList(ComTaskTpl.TOPOLOGY_UPDATE, ComTaskTpl.READ_LOAD_PROFILE_DATA, ComTaskTpl.READ_LOG_BOOK_DATA, ComTaskTpl.READ_REGISTER_DATA)),
    RTU_Plus_G3("Default", GatewayType.LOCAL_AREA_NETWORK,
        Collections.singletonList(SecurityPropertySetTpl.HIGH_LEVEL_NO_ENCRYPTION),
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST,
        Collections.singletonList(ComTaskTpl.TOPOLOGY_UPDATE))
    ;

    private String name;
    private GatewayType gatewayType;
    private List<SecurityPropertySetTpl> securityPropertySets;
    private List<RegisterTypeTpl> registerTypes;
    private List<LoadProfileTypeTpl> loadProfileTypes;
    private List<LogBookTypeTpl> logBookTypes;
    private List<ComTaskTpl> comTaskTpls;

    DeviceConfigurationTpl(String name, GatewayType gatewayType, List<SecurityPropertySetTpl> securityPropertySets,
               List<RegisterTypeTpl> registerTypes, List<LoadProfileTypeTpl> loadProfileTypes, List<LogBookTypeTpl> logBookTypes, List<ComTaskTpl> comTasks) {
        this.name = name;
        this.gatewayType = gatewayType;
        this.securityPropertySets = securityPropertySets;
        this.registerTypes = registerTypes;
        this.loadProfileTypes = loadProfileTypes;
        this.logBookTypes = logBookTypes;
        this.comTaskTpls = comTasks;
    }

    @Override
    public Class<DeviceConfigurationBuilder> getBuilderClass() {
        return DeviceConfigurationBuilder.class;
    }

    @Override
    public DeviceConfigurationBuilder get(DeviceConfigurationBuilder builder) {
        return builder.withName(this.name).withGatewayType(this.gatewayType)
                .withSecurityPropertySetBuilders(this.securityPropertySets.stream().map(Builders::from).collect(Collectors.toList()))
                .withRegisterTypes(this.registerTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()))
                .withLoadProfileTypes(this.loadProfileTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()))
                .withLogBookTypes(this.logBookTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()))
                .withComTasks(this.comTaskTpls.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()));
    }

    public String getName() {
        return name;
    }
}
