package com.elster.jupiter.metering;

import com.elster.jupiter.fsm.FiniteStateMachine;

public interface MeterBuilder {

    Meter create();

    MeterBuilder setMRID(String mRID);

    MeterBuilder setAmrId(String amrId);

    MeterBuilder setStateMachine(FiniteStateMachine finiteStateMachine);

    MeterBuilder setName(String name);

    MeterBuilder setSerialNumber(String serialNumber);

}
