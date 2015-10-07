package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.inject.Inject;

import java.math.BigDecimal;

class MultiplierValueImpl implements MultiplierValue {

    private Reference<MeterActivation> meterActivation = ValueReference.absent();
    private Reference<MultiplierType> type = ValueReference.absent();

    private BigDecimal value;

    @Inject
    MultiplierValueImpl() {

    }

    MultiplierValueImpl init(MeterActivation meterActivation, MultiplierType type, BigDecimal value) {
        this.meterActivation.set(meterActivation);
        this.type.set(type);
        this.value = value;
        return this;
    }

    static MultiplierValueImpl from(MeterActivation meterActivation, MultiplierType type, BigDecimal value) {
        return new MultiplierValueImpl().init(meterActivation, type, value);
    }

    @Override
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public void setValue(BigDecimal value) {
        this.value = value;
        update();
    }

    private void update() {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public MultiplierType getType() {
        return type.get();
    }
}
