package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.ReadingTypeBuilder;
import com.elster.jupiter.demo.impl.builders.RegisterTypeBuilder;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.masterdata.RegisterType;

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
    ;
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
}
