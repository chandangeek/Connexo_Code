package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.RegisterGroupBuilder;
import com.energyict.mdc.masterdata.RegisterGroup;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public enum RegisterGroupTpl implements Template<RegisterGroup, RegisterGroupBuilder> {
    DEVICE_DATA("Device data", Arrays.asList(
            RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E, RegisterTypeTpl.S_F_E_S_M_E_T1,
            RegisterTypeTpl.S_F_E_S_M_E_T2, RegisterTypeTpl.S_R_E_S_M_E_T1, RegisterTypeTpl.S_R_E_S_M_E_T2)),
    TARIFF_1("Tariff 1",
            Arrays.asList(RegisterTypeTpl.S_F_E_S_M_E_T1, RegisterTypeTpl.S_R_E_S_M_E_T1)),
    TARIFF_2("Tariff 2",
            Arrays.asList(RegisterTypeTpl.S_F_E_S_M_E_T2, RegisterTypeTpl.S_R_E_S_M_E_T2)),
    DATA_LOGGER_REGISTER_DATA("Data logger register data", RegisterTypeTpl.dataLoggerRegisterTypes())
    ;

    private String name;
    private Collection<RegisterTypeTpl> registerTypes;

    RegisterGroupTpl(String name, Collection<RegisterTypeTpl> registerTypes) {
        this.name = name;
        this.registerTypes = registerTypes;
    }

    @Override
    public Class<RegisterGroupBuilder> getBuilderClass() {
        return RegisterGroupBuilder.class;
    }

    @Override
    public RegisterGroupBuilder get(RegisterGroupBuilder builder) {
        return builder.withName(this.name).withRegisterTypes(this.registerTypes.stream()
                .map(rtDescr -> Builders.from(rtDescr).get()).collect(Collectors.toList()));
    }
}
