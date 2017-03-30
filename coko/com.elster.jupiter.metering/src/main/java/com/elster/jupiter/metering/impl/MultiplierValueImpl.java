/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;

class MultiplierValueImpl implements MultiplierValue {

    private final DataModel dataModel;
    private Reference<MeterActivation> meterActivation = ValueReference.absent();
    private Reference<MultiplierType> type = ValueReference.absent();

    private BigDecimal value;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    MultiplierValueImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    MultiplierValueImpl init(MeterActivation meterActivation, MultiplierType type, BigDecimal value) {
        this.meterActivation.set(meterActivation);
        this.type.set(type);
        this.value = value;
        return this;
    }

    static MultiplierValueImpl from(DataModel dataModel, MeterActivation meterActivation, MultiplierType type, BigDecimal value) {
        return dataModel.getInstance(MultiplierValueImpl.class).init(meterActivation, type, value);
    }

    @Override
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public void setValue(BigDecimal value) {
        this.value = value;
        dataModel.mapper(MultiplierValue.class).update(this, "value");
    }

    @Override
    public MultiplierType getType() {
        return type.get();
    }
}
