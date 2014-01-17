package com.energyict.mdc.protocol.api.legacy.dynamic;

public class SimpleSeed implements Seed {

    Object target;

    public SimpleSeed(Object object) {
        this.target = object;
    }

    public Object get() {
        return target;
    }

}
