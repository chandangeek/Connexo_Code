package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DeviceTypeBuilder;
import com.energyict.mdc.device.config.DeviceType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DeviceTypeTpl implements Template<DeviceType, DeviceTypeBuilder> {
    Elster_AS1440 ("Elster AS1440", "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP", 245, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.asList(RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E, RegisterTypeTpl.B_F_E_S_M_E_T1, RegisterTypeTpl.B_F_E_S_M_E_T2, RegisterTypeTpl.B_R_E_S_M_E_T1, RegisterTypeTpl.B_R_E_S_M_E_T2),
            Arrays.asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Arrays.asList(LogBookTypeTpl.GENERIC)),
    Elster_AS3000 ("Elster AS3000", "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP", 352, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.asList(RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E, RegisterTypeTpl.B_F_E_S_M_E_T1, RegisterTypeTpl.B_F_E_S_M_E_T2, RegisterTypeTpl.B_R_E_S_M_E_T1, RegisterTypeTpl.B_R_E_S_M_E_T2),
            Arrays.asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Arrays.asList(LogBookTypeTpl.GENERIC)),
    Landis_Gyr_ZMD ("Landis+Gyr ZMD", "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP", 73, OutboundTCPComPortPoolTpl.VODAFONE,
            Arrays.asList(RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E, RegisterTypeTpl.B_F_E_S_M_E_T1, RegisterTypeTpl.B_F_E_S_M_E_T2, RegisterTypeTpl.B_R_E_S_M_E_T1, RegisterTypeTpl.B_R_E_S_M_E_T2),
            Arrays.asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Arrays.asList(LogBookTypeTpl.GENERIC)),
    Actaris_SL7000 ("Actaris SL7000", "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP", 110, OutboundTCPComPortPoolTpl.VODAFONE,
            Arrays.asList(RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E, RegisterTypeTpl.B_F_E_S_M_E_T1, RegisterTypeTpl.B_F_E_S_M_E_T2, RegisterTypeTpl.B_R_E_S_M_E_T1, RegisterTypeTpl.B_R_E_S_M_E_T2),
            Arrays.asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Arrays.asList(LogBookTypeTpl.GENERIC)),
    Siemens_7ED ("Siemens 7ED", "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP", 96, OutboundTCPComPortPoolTpl.VODAFONE,
            Arrays.asList(RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E, RegisterTypeTpl.B_F_E_S_M_E_T1, RegisterTypeTpl.B_F_E_S_M_E_T2, RegisterTypeTpl.B_R_E_S_M_E_T1, RegisterTypeTpl.B_R_E_S_M_E_T2),
            Arrays.asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Arrays.asList(LogBookTypeTpl.GENERIC)),
    Iskra_38 ("Iskra 382", "com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP", 84, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.asList(RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E, RegisterTypeTpl.B_F_E_S_M_E_T1, RegisterTypeTpl.B_F_E_S_M_E_T2, RegisterTypeTpl.B_R_E_S_M_E_T1, RegisterTypeTpl.B_R_E_S_M_E_T2),
            Arrays.asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Arrays.asList(LogBookTypeTpl.GENERIC)),
    Alpha_A3 ("ALPHA_A3", "com.energyict.protocolimpl.elster.a3.AlphaA3", 1, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.asList(RegisterTypeTpl.BULK_A_PLUS_ALL_PHASES, RegisterTypeTpl.BULK_A_MINUS_ALL_PHASES, RegisterTypeTpl.BULK_REACTIVE_ENERGY_PLUS, RegisterTypeTpl.BULK_REACTIVE_ENERGY_MINUS),
            Arrays.asList(LoadProfileTypeTpl.ELSTER_A3_GENERIC),
            Arrays.asList(LogBookTypeTpl.GENERIC)),
    ;

    private String name;
    private String protocol;
    private int deviceCount;
    private OutboundTCPComPortPoolTpl poolTpl;

    private List<RegisterTypeTpl> registerTypes;
    private List<LoadProfileTypeTpl> loadProfileTypes;
    private List<LogBookTypeTpl> logBookTypes;

    DeviceTypeTpl(String name, String protocol, int deviceCount, OutboundTCPComPortPoolTpl poolTpl, List<RegisterTypeTpl> registerTypes, List<LoadProfileTypeTpl> loadProfileTypes, List<LogBookTypeTpl> logBookTypes) {
        this.name = name;
        this.protocol = protocol;
        this.deviceCount = deviceCount;
        this.poolTpl = poolTpl;
        this.registerTypes = registerTypes;
        this.loadProfileTypes = loadProfileTypes;
        this.logBookTypes = logBookTypes;
    }

    @Override
    public Class<DeviceTypeBuilder> getBuilderClass(){
        return DeviceTypeBuilder.class;
    }

    @Override
    public DeviceTypeBuilder get(DeviceTypeBuilder builder){
        return builder.withName(this.name).withProtocol(this.protocol)
                .withRegisterTypes(this.registerTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()))
                .withLoadProfileTypes(this.loadProfileTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()))
                .withLogBookTypes(this.logBookTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()));
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public OutboundTCPComPortPoolTpl getPoolTpl() {
        return poolTpl;
    }

    public String getName() {
        return name;
    }
}
