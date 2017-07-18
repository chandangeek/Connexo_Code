/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.RegisterGroupBuilder;
import com.energyict.mdc.masterdata.RegisterGroup;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public enum RegisterGroupTpl implements Template<RegisterGroup, RegisterGroupBuilder> {
    DEVICE_DATA("Device data", Arrays.asList(
            RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1,
            RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2)),
    TARIFF_1("Tariff 1",
            Arrays.asList(RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1)),
    TARIFF_2("Tariff 2",
            Arrays.asList(RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2)),
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
