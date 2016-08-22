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
            RegisterTypeTpl.S_F_E_S_M_E_T1, RegisterTypeTpl.S_F_E_S_M_E_T2, RegisterTypeTpl.S_R_E_S_M_E_T1, RegisterTypeTpl.S_R_E_S_M_E_T2),
    MONTHLY_ELECTRICITY("Monthly Electricity", "0.0.98.1.0.255", TimeDuration.months(1),
            RegisterTypeTpl.S_F_E_S_M_E_T1, RegisterTypeTpl.S_F_E_S_M_E_T2, RegisterTypeTpl.S_R_E_S_M_E_T1, RegisterTypeTpl.S_R_E_S_M_E_T2),
    _15_MIN_ELECTRICITY("15min Electricity", "1.0.99.1.0.255", TimeDuration.minutes(15),
            RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E),
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
