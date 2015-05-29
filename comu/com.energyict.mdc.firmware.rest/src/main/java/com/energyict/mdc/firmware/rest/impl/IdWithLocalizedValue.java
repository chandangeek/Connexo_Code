package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;

public class IdWithLocalizedValue<T> {
    public T id;
    public String localizedValue;

    public IdWithLocalizedValue(){}

    public IdWithLocalizedValue(T id, String localizedValue) {
        this.id = id;
        this.localizedValue = localizedValue;
    }

    public static <S extends HasId & HasName>IdWithLocalizedValue<Long> from(S obj){
        return new IdWithLocalizedValue<>(obj.getId(), obj.getName());
    }
}
