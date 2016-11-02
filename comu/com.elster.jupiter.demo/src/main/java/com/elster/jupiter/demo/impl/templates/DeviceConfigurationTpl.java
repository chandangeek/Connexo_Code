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
    PROSUMERS("Prosumers", GatewayType.HOME_AREA_NETWORK,
            Arrays.<SecurityPropertySetTpl>asList(SecurityPropertySetTpl.NO_SECURITY, SecurityPropertySetTpl.HIGH_LEVEL),
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG),
            Arrays.asList(ComTaskTpl.excludeTopologyTpls())),
    PROSUMERS_VALIDATION_STRICT("Prosumers strict", GatewayType.HOME_AREA_NETWORK,
            Arrays.<SecurityPropertySetTpl>asList(SecurityPropertySetTpl.NO_SECURITY, SecurityPropertySetTpl.HIGH_LEVEL),
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG),
            Arrays.asList(ComTaskTpl.excludeTopologyTpls())),
    CONSUMERS("Consumers", GatewayType.HOME_AREA_NETWORK,
            Arrays.<SecurityPropertySetTpl>asList(SecurityPropertySetTpl.NO_SECURITY, SecurityPropertySetTpl.HIGH_LEVEL),
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG),
            Arrays.asList(ComTaskTpl.excludeTopologyTpls())),
    AM540("Default", GatewayType.HOME_AREA_NETWORK,
            Collections.emptyList(),
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Collections.singletonList(LogBookTypeTpl.GENERIC),
            Arrays.<ComTaskTpl>asList(ComTaskTpl.READ_LOAD_PROFILE_DATA, ComTaskTpl.READ_LOG_BOOK_DATA, ComTaskTpl.READ_REGISTER_DATA)),
    RTU_Plus_G3("Default", GatewayType.LOCAL_AREA_NETWORK,
            Collections.singletonList(SecurityPropertySetTpl.HIGH_LEVEL_NO_ENCRYPTION_GMAC),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.singletonList(ComTaskTpl.TOPOLOGY_UPDATE)),
    DATA_LOGGER("Default", GatewayType.HOME_AREA_NETWORK,
            Arrays.asList(SecurityPropertySetTpl.NO_SECURITY, SecurityPropertySetTpl.HIGH_LEVEL_NO_ENCRYPTION_MD5),
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.DATA_LOGGER_1, RegisterTypeTpl.DATA_LOGGER_2, RegisterTypeTpl.DATA_LOGGER_3, RegisterTypeTpl.DATA_LOGGER_4, RegisterTypeTpl.DATA_LOGGER_5,
                    RegisterTypeTpl.DATA_LOGGER_6, RegisterTypeTpl.DATA_LOGGER_7, RegisterTypeTpl.DATA_LOGGER_8, RegisterTypeTpl.DATA_LOGGER_9, RegisterTypeTpl.DATA_LOGGER_10,
                    RegisterTypeTpl.DATA_LOGGER_11, RegisterTypeTpl.DATA_LOGGER_12, RegisterTypeTpl.DATA_LOGGER_13, RegisterTypeTpl.DATA_LOGGER_14, RegisterTypeTpl.DATA_LOGGER_15,
                    RegisterTypeTpl.DATA_LOGGER_16, RegisterTypeTpl.DATA_LOGGER_17, RegisterTypeTpl.DATA_LOGGER_18, RegisterTypeTpl.DATA_LOGGER_19, RegisterTypeTpl.DATA_LOGGER_20,
                    RegisterTypeTpl.DATA_LOGGER_21, RegisterTypeTpl.DATA_LOGGER_22, RegisterTypeTpl.DATA_LOGGER_23, RegisterTypeTpl.DATA_LOGGER_24, RegisterTypeTpl.DATA_LOGGER_25,
                    RegisterTypeTpl.DATA_LOGGER_26, RegisterTypeTpl.DATA_LOGGER_27, RegisterTypeTpl.DATA_LOGGER_28, RegisterTypeTpl.DATA_LOGGER_29, RegisterTypeTpl.DATA_LOGGER_30,
                    RegisterTypeTpl.DATA_LOGGER_31, RegisterTypeTpl.DATA_LOGGER_32
            ),
            Collections.singletonList(com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl.DATA_LOGGER_32),
            Collections.emptyList(),
            Arrays.asList(ComTaskTpl.READ_DATA_LOGGER_REGISTER_DATA, ComTaskTpl.READ_DATA_LOGGER_LOAD_PROFILE_DATA)),
    DATA_LOGGER_SLAVE("15min Electricity", GatewayType.NONE,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.singletonList(LoadProfileTypeTpl._15_MIN_ELECTRICITY),
            null, null);

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
        builder.withName(this.name).withGatewayType(this.gatewayType)
                .withSecurityPropertySetBuilders(this.securityPropertySets.stream().map(Builders::from).collect(Collectors.toList()))
                .withRegisterTypes(this.registerTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()))
                .withLoadProfileTypes(this.loadProfileTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()));
        if (this.logBookTypes != null) {
            builder.withLogBookTypes(this.logBookTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()));
        }
        if (this.comTaskTpls != null) {
            builder.withComTasks(this.comTaskTpls.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()));
        }
        return builder;
    }

    public String getName() {
        return name;
    }
}
