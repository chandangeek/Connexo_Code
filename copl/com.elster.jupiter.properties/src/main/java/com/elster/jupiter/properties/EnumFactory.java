/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.util.stream.Stream;

/**
 * Created by antfom on 03.08.2015.
 */
public class EnumFactory extends AbstractValueFactory<Enum> {
    private final Class<? extends Enum> enumClass;

    public EnumFactory(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public Enum fromStringValue(String stringValue) {
        return Stream.of(enumClass.getEnumConstants())
                .filter(e -> stringValue.equalsIgnoreCase(e.name()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String toStringValue(Enum object) {
        return object.toString();
    }

    @Override
    public Class<Enum> getValueType() {
        return Enum.class;
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public Enum valueFromDatabase(Object object) {
        return (Enum) object;
    }

    @Override
    public Object valueToDatabase(Enum object) {
        return object;
    }
}
