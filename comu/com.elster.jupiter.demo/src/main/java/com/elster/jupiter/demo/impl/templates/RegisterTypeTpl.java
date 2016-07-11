package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.ReadingTypeBuilder;
import com.elster.jupiter.demo.impl.builders.RegisterTypeBuilder;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.masterdata.RegisterType;

import java.util.EnumSet;
import java.util.stream.Collectors;

public enum RegisterTypeTpl implements Template<RegisterType, RegisterTypeBuilder> {
    // Base registers
    B_F_E_S_M_E ("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "1.0.1.8.0.255"),
    B_R_E_S_M_E ("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0", "1.0.2.8.0.255"),
    S_F_E_S_M_E_T1("0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0", "1.0.1.8.1.255"),
    S_F_E_S_M_E_T2("0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0", "1.0.2.8.1.255"),
    S_R_E_S_M_E_T1("0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0", "1.0.1.8.2.255"),
    S_R_E_S_M_E_T2("0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0", "1.0.2.8.2.255"),

    // A3 register types
    DELTA_A_PLUS_ALL_PHASES("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "1.0.1.6.0.255"),
    DELTA_A_MINUS_ALL_PHASES("0.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "1.0.2.6.0.255"),
    DELRA_REACTIVE_ENERGY_PLUS("0.0.0.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "1.0.3.6.0.255"),
    DELRA_REACTIVE_ENERGY_MINUS("0.0.0.4.3.1.12.0.0.0.0.0.0.0.0.3.73.0","1.0.4.6.0.255"),

    BULK_A_PLUS_ALL_PHASES("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "1.0.1.8.0.255"),
    BULK_A_MINUS_ALL_PHASES("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "1.0.2.8.0.255"),
    BULK_REACTIVE_ENERGY_PLUS("0.0.0.1.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "1.0.3.8.0.255"),
    BULK_REACTIVE_ENERGY_MINUS("0.0.0.1.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "1.0.4.8.0.255"),

    // Data logger register types  (Pulse
    DATA_LOGGER_1("0.0.0.1.0.0.142.0.0.1.1.0.0.0.0.0.111.0", "1.1.128.8.0.255"),
    DATA_LOGGER_2("0.0.0.1.0.0.142.0.0.2.1.0.0.0.0.0.111.0", "1.2.128.8.0.255"),
    DATA_LOGGER_3("0.0.0.1.0.0.142.0.0.3.1.0.0.0.0.0.111.0", "1.3.128.8.0.255"),
    DATA_LOGGER_4("0.0.0.1.0.0.142.0.0.4.1.0.0.0.0.0.111.0", "1.4.128.8.0.255"),
    DATA_LOGGER_5("0.0.0.1.0.0.142.0.0.5.1.0.0.0.0.0.111.0", "1.5.128.8.0.255"),
    DATA_LOGGER_6("0.0.0.1.0.0.142.0.0.6.1.0.0.0.0.0.111.0", "1.6.128.8.0.255"),
    DATA_LOGGER_7("0.0.0.1.0.0.142.0.0.7.1.0.0.0.0.0.111.0", "1.7.128.8.0.255"),
    DATA_LOGGER_8("0.0.0.1.0.0.142.0.0.8.1.0.0.0.0.0.111.0", "1.8.128.8.0.255"),
    DATA_LOGGER_9("0.0.0.1.0.0.142.0.0.9.1.0.0.0.0.0.111.0", "1.9.128.8.0.255"),
    DATA_LOGGER_10("0.0.0.1.0.0.142.0.0.10.1.0.0.0.0.0.111.0", "1.10.128.8.0.255"),
    DATA_LOGGER_11("0.0.0.1.0.0.142.0.0.11.1.0.0.0.0.0.111.0", "1.11.128.8.0.255"),
    DATA_LOGGER_12("0.0.0.1.0.0.142.0.0.12.1.0.0.0.0.0.111.0", "1.12.128.8.0.255"),
    DATA_LOGGER_13("0.0.0.1.0.0.142.0.0.13.1.0.0.0.0.0.111.0", "1.13.128.8.0.255"),
    DATA_LOGGER_14("0.0.0.1.0.0.142.0.0.14.1.0.0.0.0.0.111.0", "1.14.128.8.0.255"),
    DATA_LOGGER_15("0.0.0.1.0.0.142.0.0.15.1.0.0.0.0.0.111.0", "1.15.128.8.0.255"),
    DATA_LOGGER_16("0.0.0.1.0.0.142.0.0.16.1.0.0.0.0.0.111.0", "1.16.128.8.0.255"),
    DATA_LOGGER_17("0.0.0.1.0.0.142.0.0.17.1.0.0.0.0.0.111.0", "1.17.128.8.0.255"),
    DATA_LOGGER_18("0.0.0.1.0.0.142.0.0.18.1.0.0.0.0.0.111.0", "1.18.128.8.0.255"),
    DATA_LOGGER_19("0.0.0.1.0.0.142.0.0.19.1.0.0.0.0.0.111.0", "1.19.128.8.0.255"),
    DATA_LOGGER_20("0.0.0.1.0.0.142.0.0.20.1.0.0.0.0.0.111.0", "1.20.128.8.0.255"),
    DATA_LOGGER_21("0.0.0.1.0.0.142.0.0.21.1.0.0.0.0.0.111.0", "1.21.128.8.0.255"),
    DATA_LOGGER_22("0.0.0.1.0.0.142.0.0.22.1.0.0.0.0.0.111.0", "1.22.128.8.0.255"),
    DATA_LOGGER_23("0.0.0.1.0.0.142.0.0.23.1.0.0.0.0.0.111.0", "1.23.128.8.0.255"),
    DATA_LOGGER_24("0.0.0.1.0.0.142.0.0.24.1.0.0.0.0.0.111.0", "1.24.128.8.0.255"),
    DATA_LOGGER_25("0.0.0.1.0.0.142.0.0.25.1.0.0.0.0.0.111.0", "1.25.128.8.0.255"),
    DATA_LOGGER_26("0.0.0.1.0.0.142.0.0.26.1.0.0.0.0.0.111.0", "1.26.128.8.0.255"),
    DATA_LOGGER_27("0.0.0.1.0.0.142.0.0.27.1.0.0.0.0.0.111.0", "1.27.128.8.0.255"),
    DATA_LOGGER_28("0.0.0.1.0.0.142.0.0.28.1.0.0.0.0.0.111.0", "1.28.128.8.0.255"),
    DATA_LOGGER_29("0.0.0.1.0.0.142.0.0.29.1.0.0.0.0.0.111.0", "1.29.128.8.0.255"),
    DATA_LOGGER_30("0.0.0.1.0.0.142.0.0.30.1.0.0.0.0.0.111.0", "1.30.128.8.0.255"),
    DATA_LOGGER_31("0.0.0.1.0.0.142.0.0.31.1.0.0.0.0.0.111.0", "1.31.128.8.0.255"),
    DATA_LOGGER_32("0.0.0.1.0.0.142.0.0.32.1.0.0.0.0.0.111.0", "1.32.128.8.0.255");

    private String obisCode;
    private String mrid;

    RegisterTypeTpl(String mrid, String obisCode) {
        this.obisCode = obisCode;
        this.mrid = mrid;
    }

    public String getMrid(){
        return mrid;
    }

    public String getObisCode(){
        return obisCode;
    }

    @Override
    public Class<RegisterTypeBuilder> getBuilderClass() {
        return RegisterTypeBuilder.class;
    }

    @Override
    public RegisterTypeBuilder get(RegisterTypeBuilder builder) {
        ReadingType readingType = Builders.from(ReadingTypeBuilder.class).withMrid(mrid).find()
                .orElseThrow(() -> new UnableToCreate("Unable to find reading type with mrid '"+ mrid + "'"));
        return builder.withObisCode(obisCode).withReadingType(readingType);
    }

    public static EnumSet<RegisterTypeTpl> dataLoggerRegisterTypes(){
        return EnumSet.copyOf(
                EnumSet.allOf(RegisterTypeTpl.class)
                        .stream()
                        .filter((tpl)->tpl.name().startsWith("DATA_LOGGER"))
                        .collect(Collectors.toSet())
        );

    }
}
