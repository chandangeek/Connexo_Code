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
