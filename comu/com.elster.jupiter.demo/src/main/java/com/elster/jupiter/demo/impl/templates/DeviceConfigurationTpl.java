/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
            Arrays.asList(ComTaskTpl.excludeTopologyTpls()), true),
    CONSUMERS("Consumers", GatewayType.HOME_AREA_NETWORK,
            Arrays.<SecurityPropertySetTpl>asList(SecurityPropertySetTpl.NO_SECURITY, SecurityPropertySetTpl.HIGH_LEVEL),
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG),
            Arrays.asList(ComTaskTpl.excludeTopologyTpls()), true),
    AM540("Default", GatewayType.HOME_AREA_NETWORK,
            Collections.emptyList(),
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Collections.singletonList(LogBookTypeTpl.GENERIC),
            Arrays.<ComTaskTpl>asList(ComTaskTpl.READ_LOAD_PROFILE_DATA, ComTaskTpl.READ_LOG_BOOK_DATA, ComTaskTpl.READ_REGISTER_DATA), true),
    RTU_Plus_G3("Default", GatewayType.LOCAL_AREA_NETWORK,
            Collections.singletonList(SecurityPropertySetTpl.HIGH_LEVEL_NO_ENCRYPTION_GMAC),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.singletonList(ComTaskTpl.TOPOLOGY_UPDATE), true),
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
            Arrays.asList(ComTaskTpl.READ_DATA_LOGGER_REGISTER_DATA, ComTaskTpl.READ_DATA_LOGGER_LOAD_PROFILE_DATA), true),
    DATA_LOGGER_SLAVE("15min Electricity", GatewayType.NONE,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.singletonList(LoadProfileTypeTpl._15_MIN_ELECTRICITY),
            null, null, true),
    MULTI_ELEMENT_DEVICE("Default", GatewayType.HOME_AREA_NETWORK,
            Arrays.asList(SecurityPropertySetTpl.NO_SECURITY, SecurityPropertySetTpl.HIGH_LEVEL_NO_ENCRYPTION_MD5),
            Arrays.<RegisterTypeTpl>asList(com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_1, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_2, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_3, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_4, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_5,
                                           com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_6, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_7, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_8, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_9, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_10,
                                           com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_11, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_12, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_13, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_14, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_15,
                                           com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_16, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_17, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_18, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_19, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_20,
                                           com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_21, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_22, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_23, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_24, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_25,
                                           com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_26, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_27, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_28, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_29, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_30,
                                           com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_31, com.elster.jupiter.demo.impl.templates.RegisterTypeTpl.DATA_LOGGER_32
                                           ),
            Collections.singletonList(com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl.DATA_LOGGER_32),
            Collections.emptyList(),
            Arrays.asList(ComTaskTpl.READ_DATA_LOGGER_REGISTER_DATA, ComTaskTpl.READ_DATA_LOGGER_LOAD_PROFILE_DATA), true),
    DEFAULT_GAS("Default", GatewayType.NONE,
            Collections.singletonList(SecurityPropertySetTpl.NO_SECURITY),
            Collections.singletonList(RegisterTypeTpl.BULK_GAS_VOLUME),
            Arrays.asList(LoadProfileTypeTpl.HOURLY_GAS,LoadProfileTypeTpl.DAILY_GAS,LoadProfileTypeTpl.MONTHLY_GAS),
            Collections.emptyList(),
            Arrays.asList(ComTaskTpl.BASIC_CHECK, ComTaskTpl.READ_LOAD_PROFILE_DATA_GAS, ComTaskTpl.READ_REGISTER_DATA_GAS), false),
    DEFAULT_WATER("Default", GatewayType.NONE,
            Collections.singletonList(SecurityPropertySetTpl.NO_SECURITY),
            Collections.singletonList(RegisterTypeTpl.BULK_WATER_VOLUME),
            Arrays.asList(LoadProfileTypeTpl.HOURLY_WATER,LoadProfileTypeTpl.DAILY_WATER,LoadProfileTypeTpl.MONTHLY_WATER),
            Collections.emptyList(),
            Arrays.asList(ComTaskTpl.BASIC_CHECK, ComTaskTpl.READ_LOAD_PROFILE_DATA_WATER, ComTaskTpl.READ_REGISTER_DATA_WATER), false)
    ;

    private String name;
    private GatewayType gatewayType;
    private List<SecurityPropertySetTpl> securityPropertySets;
    private List<RegisterTypeTpl> registerTypes;
    private List<LoadProfileTypeTpl> loadProfileTypes;
    private List<LogBookTypeTpl> logBookTypes;
    private List<ComTaskTpl> comTaskTpls;
    private boolean directlyAdressable;

    DeviceConfigurationTpl(String name, GatewayType gatewayType, List<SecurityPropertySetTpl> securityPropertySets,
                           List<RegisterTypeTpl> registerTypes, List<LoadProfileTypeTpl> loadProfileTypes, List<LogBookTypeTpl> logBookTypes, List<ComTaskTpl> comTasks, boolean directlyAddressable) {
        this.name = name;
        this.gatewayType = gatewayType;
        this.securityPropertySets = securityPropertySets;
        this.registerTypes = registerTypes;
        this.loadProfileTypes = loadProfileTypes;
        this.logBookTypes = logBookTypes;
        this.comTaskTpls = comTasks;
        this.directlyAdressable = directlyAddressable;
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
                .withLoadProfileTypes(this.loadProfileTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()))
                .withDirectlyAddressable(this.directlyAdressable);
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
