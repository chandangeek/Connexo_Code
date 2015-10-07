package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MultiplierType;

class MultiplierTypeImpl implements MultiplierType {

    private String name;

    MultiplierTypeImpl() {
    }

    MultiplierTypeImpl init(String name) {
        this.name = name;
        return this;
    }

    static MultiplierTypeImpl from(String name) {
        return new MultiplierTypeImpl().init(name);
    }

    @Override
    public String getName() {
        return name;
    }
}
