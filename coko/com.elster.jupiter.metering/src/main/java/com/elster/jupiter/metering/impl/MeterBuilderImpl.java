package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterBuilder;

import javax.inject.Provider;

class MeterBuilderImpl implements MeterBuilder {

    private final AmrSystem amrSystem;
    private final Provider<MeterImpl> meterFactory;
    private String amrId;
    private String mRID;
    private String name;
    private FiniteStateMachine finiteStateMachine;
    private String serialNumber;

    MeterBuilderImpl(AmrSystem amrSystem, Provider<MeterImpl> meterFactory, String amrId) {
        this.amrSystem = amrSystem;
        this.meterFactory = meterFactory;
        this.amrId = amrId;
    }

    @Override
    public Meter create() {
        MeterImpl meter = meterFactory.get().init(amrSystem, amrId, mRID);
        meter.setName(name);
        if (finiteStateMachine != null) {
            meter.setFiniteStateMachine(finiteStateMachine);
        }
        meter.setSerialNumber(serialNumber);
        meter.doSave();
        return meter;
    }

    @Override
    public MeterBuilder setMRID(String mRID) {
        this.mRID = mRID;
        return this;
    }

    @Override
    public MeterBuilder setAmrId(String amrId) {
        this.amrId = amrId;
        return this;
    }

    @Override
    public MeterBuilder setStateMachine(FiniteStateMachine finiteStateMachine) {
        this.finiteStateMachine = finiteStateMachine;
        return this;
    }

    @Override
    public MeterBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public MeterBuilder setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }
}
