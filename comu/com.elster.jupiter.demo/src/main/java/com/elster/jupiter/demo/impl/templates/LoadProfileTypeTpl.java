package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.LoadProfileTypeBuilder;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;

import java.util.ArrayList;
import java.util.List;

public enum LoadProfileTypeTpl implements Template<LoadProfileType, LoadProfileTypeBuilder> {
    DAILY_ELECTRICITY("Daily Electricity", "1.0.99.2.0.255", TimeDuration.days(1),
            RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
    DAILY_ELECTRICITY_A_PLUS("Daily Electricity A+", "1.0.99.2.0.255", TimeDuration.days(1),
            RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2),
    MONTHLY_ELECTRICITY("Monthly Electricity", "0.0.98.1.0.255", TimeDuration.months(1),
            RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
    MONTHLY_ELECTRICITY_A_PLUS("Monthly Electricity A+", "0.0.98.1.0.255", TimeDuration.months(1),
            RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2),
    _15_MIN_ELECTRICITY("15min Electricity", "1.0.99.1.0.255", TimeDuration.minutes(15),
            RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS),
    _15_MIN_ELECTRICITY_A_PLUS("15min Electricity A+", "1.0.99.1.0.255", TimeDuration.minutes(15),
            RegisterTypeTpl.SECONDARY_BULK_A_PLUS),
    ELSTER_A3_GENERIC("Elster A3 Generic [15min]", "0.15.99.1.0.255", TimeDuration.minutes(15),
            RegisterTypeTpl.DELTA_A_PLUS_ALL_PHASES, RegisterTypeTpl.DELTA_A_MINUS_ALL_PHASES, RegisterTypeTpl.DELRA_REACTIVE_ENERGY_PLUS, RegisterTypeTpl.DELRA_REACTIVE_ENERGY_MINUS),
    DATA_LOGGER_32("Data Logger (32)",  "0.0.128.8.0.255", TimeDuration.minutes(15),
            RegisterTypeTpl.DATA_LOGGER_1, RegisterTypeTpl.DATA_LOGGER_2, RegisterTypeTpl.DATA_LOGGER_3, RegisterTypeTpl.DATA_LOGGER_4,
            RegisterTypeTpl.DATA_LOGGER_5, RegisterTypeTpl.DATA_LOGGER_6, RegisterTypeTpl.DATA_LOGGER_7, RegisterTypeTpl.DATA_LOGGER_8,
            RegisterTypeTpl.DATA_LOGGER_9, RegisterTypeTpl.DATA_LOGGER_10, RegisterTypeTpl.DATA_LOGGER_11, RegisterTypeTpl.DATA_LOGGER_12,
            RegisterTypeTpl.DATA_LOGGER_13, RegisterTypeTpl.DATA_LOGGER_14, RegisterTypeTpl.DATA_LOGGER_15, RegisterTypeTpl.DATA_LOGGER_16,
            RegisterTypeTpl.DATA_LOGGER_17, RegisterTypeTpl.DATA_LOGGER_18, RegisterTypeTpl.DATA_LOGGER_19, RegisterTypeTpl.DATA_LOGGER_20,
            RegisterTypeTpl.DATA_LOGGER_21, RegisterTypeTpl.DATA_LOGGER_22, RegisterTypeTpl.DATA_LOGGER_23, RegisterTypeTpl.DATA_LOGGER_24,
            RegisterTypeTpl.DATA_LOGGER_25, RegisterTypeTpl.DATA_LOGGER_26, RegisterTypeTpl.DATA_LOGGER_27, RegisterTypeTpl.DATA_LOGGER_28,
            RegisterTypeTpl.DATA_LOGGER_29, RegisterTypeTpl.DATA_LOGGER_30, RegisterTypeTpl.DATA_LOGGER_31, RegisterTypeTpl.DATA_LOGGER_32),
    ;
    private String name;
    private String obisCode;
    private TimeDuration timeDuration;
    private RegisterTypeTpl[] registerTypes;

    LoadProfileTypeTpl(String name, String obisCode, TimeDuration timeDuration, RegisterTypeTpl... registerTypes) {
        this.name = name;
        this.obisCode = obisCode;
        this.timeDuration = timeDuration;
        this.registerTypes = registerTypes;
    }

    @Override
    public Class<LoadProfileTypeBuilder> getBuilderClass() {
        return LoadProfileTypeBuilder.class;
    }

    @Override
    public LoadProfileTypeBuilder get(LoadProfileTypeBuilder builder) {
        List<RegisterType> registerTypes = new ArrayList<>();
        for (RegisterTypeTpl rtDesc : this.registerTypes) {
            registerTypes.add(Builders.from(rtDesc).get());
        }
        return builder.withName(this.name).withObisCode(this.obisCode).withTimeDuration(this.timeDuration).withRegisters(registerTypes);
    }

    public String getName() {
        return name;
    }

    public String getObisCode() {
        return obisCode;
    }

    public TimeDuration getInterval() {
        return timeDuration;
    }

    public RegisterTypeTpl[] getRegisterTypes() {
        return registerTypes;
    }
}
