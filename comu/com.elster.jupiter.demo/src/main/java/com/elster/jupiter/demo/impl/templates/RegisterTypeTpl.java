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
    SECONDARY_BULK_A_PLUS("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "1.0.1.8.0.255"),
    SECONDARY_BULK_A_MINUS("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0", "1.0.2.8.0.255"),
    SECONDARY_SUM_A_PLUS_TOU_1("0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0", "1.0.1.8.1.255"),
    SECONDARY_SUM_A_PLUS_TOU_2("0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0", "1.0.2.8.1.255"),
    SECONDARY_SUM_A_MINUS_TOU_1("0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0", "1.0.1.8.2.255"),
    SECONDARY_SUM_A_MINUS_TOU_2("0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0", "1.0.2.8.2.255"),

    // A3 register types
    DELTA_A_PLUS_ALL_PHASES("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "1.0.1.6.0.255"),
    DELTA_A_MINUS_ALL_PHASES("0.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "1.0.2.6.0.255"),
    DELRA_REACTIVE_ENERGY_PLUS("0.0.0.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "1.0.3.6.0.255"),
    DELRA_REACTIVE_ENERGY_MINUS("0.0.0.4.3.1.12.0.0.0.0.0.0.0.0.3.73.0","1.0.4.6.0.255"),

    BULK_A_PLUS_ALL_PHASES("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "1.0.1.8.0.255"),
    BULK_A_MINUS_ALL_PHASES("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "1.0.2.8.0.255"),
    BULK_REACTIVE_ENERGY_PLUS("0.0.0.1.2.1.12.0.0.0.0.0.0.0.0.3.73.0", "1.0.3.8.0.255"),
    BULK_REACTIVE_ENERGY_MINUS("0.0.0.1.3.1.12.0.0.0.0.0.0.0.0.3.73.0", "1.0.4.8.0.255"),

    // Data logger register types
    DATA_LOGGER_1("0.0.0.1.0.0.142.0.0.1.1.0.0.0.0.0.111.0", "1.0.128.8.1.255"),
    DATA_LOGGER_2("0.0.0.1.0.0.142.0.0.2.1.0.0.0.0.0.111.0", "1.0.128.8.2.255"),
    DATA_LOGGER_3("0.0.0.1.0.0.142.0.0.3.1.0.0.0.0.0.111.0", "1.0.128.8.3.255"),
    DATA_LOGGER_4("0.0.0.1.0.0.142.0.0.4.1.0.0.0.0.0.111.0", "1.0.128.8.4.255"),
    DATA_LOGGER_5("0.0.0.1.0.0.142.0.0.5.1.0.0.0.0.0.111.0", "1.0.128.8.5.255"),
    DATA_LOGGER_6("0.0.0.1.0.0.142.0.0.6.1.0.0.0.0.0.111.0", "1.0.128.8.6.255"),
    DATA_LOGGER_7("0.0.0.1.0.0.142.0.0.7.1.0.0.0.0.0.111.0", "1.0.128.8.7.255"),
    DATA_LOGGER_8("0.0.0.1.0.0.142.0.0.8.1.0.0.0.0.0.111.0", "1.0.128.8.8.255"),
    DATA_LOGGER_9("0.0.0.1.0.0.142.0.0.9.1.0.0.0.0.0.111.0", "1.0.128.8.9.255"),
    DATA_LOGGER_10("0.0.0.1.0.0.142.0.0.10.1.0.0.0.0.0.111.0", "1.0.128.8.10.255"),
    DATA_LOGGER_11("0.0.0.1.0.0.142.0.0.11.1.0.0.0.0.0.111.0", "1.0.128.8.11.255"),
    DATA_LOGGER_12("0.0.0.1.0.0.142.0.0.12.1.0.0.0.0.0.111.0", "1.0.128.8.12.255"),
    DATA_LOGGER_13("0.0.0.1.0.0.142.0.0.13.1.0.0.0.0.0.111.0", "1.0.128.8.13.255"),
    DATA_LOGGER_14("0.0.0.1.0.0.142.0.0.14.1.0.0.0.0.0.111.0", "1.0.128.8.14.255"),
    DATA_LOGGER_15("0.0.0.1.0.0.142.0.0.15.1.0.0.0.0.0.111.0", "1.0.128.8.15.255"),
    DATA_LOGGER_16("0.0.0.1.0.0.142.0.0.16.1.0.0.0.0.0.111.0", "1.0.128.8.16.255"),
    DATA_LOGGER_17("0.0.0.1.0.0.142.0.0.17.1.0.0.0.0.0.111.0", "1.0.128.8.17.255"),
    DATA_LOGGER_18("0.0.0.1.0.0.142.0.0.18.1.0.0.0.0.0.111.0", "1.0.128.8.18.255"),
    DATA_LOGGER_19("0.0.0.1.0.0.142.0.0.19.1.0.0.0.0.0.111.0", "1.0.128.8.19.255"),
    DATA_LOGGER_20("0.0.0.1.0.0.142.0.0.20.1.0.0.0.0.0.111.0", "1.0.128.8.20.255"),
    DATA_LOGGER_21("0.0.0.1.0.0.142.0.0.21.1.0.0.0.0.0.111.0", "1.0.128.8.21.255"),
    DATA_LOGGER_22("0.0.0.1.0.0.142.0.0.22.1.0.0.0.0.0.111.0", "1.0.128.8.22.255"),
    DATA_LOGGER_23("0.0.0.1.0.0.142.0.0.23.1.0.0.0.0.0.111.0", "1.0.128.8.23.255"),
    DATA_LOGGER_24("0.0.0.1.0.0.142.0.0.24.1.0.0.0.0.0.111.0", "1.0.128.8.24.255"),
    DATA_LOGGER_25("0.0.0.1.0.0.142.0.0.25.1.0.0.0.0.0.111.0", "1.0.128.8.25.255"),
    DATA_LOGGER_26("0.0.0.1.0.0.142.0.0.26.1.0.0.0.0.0.111.0", "1.0.128.8.26.255"),
    DATA_LOGGER_27("0.0.0.1.0.0.142.0.0.27.1.0.0.0.0.0.111.0", "1.0.128.8.27.255"),
    DATA_LOGGER_28("0.0.0.1.0.0.142.0.0.28.1.0.0.0.0.0.111.0", "1.0.128.8.28.255"),
    DATA_LOGGER_29("0.0.0.1.0.0.142.0.0.29.1.0.0.0.0.0.111.0", "1.0.128.8.29.255"),
    DATA_LOGGER_30("0.0.0.1.0.0.142.0.0.30.1.0.0.0.0.0.111.0", "1.0.128.8.30.255"),
    DATA_LOGGER_31("0.0.0.1.0.0.142.0.0.31.1.0.0.0.0.0.111.0", "1.0.128.8.31.255"),
    DATA_LOGGER_32("0.0.0.1.0.0.142.0.0.32.1.0.0.0.0.0.111.0", "1.0.128.8.32.255");

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
