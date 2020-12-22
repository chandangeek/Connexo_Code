package com.energyict.protocolimplv2.dlms.common.obis.matchers;

import com.energyict.obis.ObisCode;

public abstract class GenricMatcher<T> implements Matcher<T> {

    private final T object;

    public GenricMatcher(T object) {
        this.object = object;
    }

    @Override
    public boolean matches(T o) {
        return this.object.equals(o);
    }

    @Override
    public ObisCode map(ObisCode obisCode) {
        return obisCode;
    }
}
