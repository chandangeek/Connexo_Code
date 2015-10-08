package com.elster.jupiter.metering;

import com.elster.jupiter.fsm.FiniteStateMachine;

public interface EndDeviceBuilder {

    EndDevice create();

    EndDeviceBuilder setMRID(String mRID);

    EndDeviceBuilder setAmrId(String amrId);

    EndDeviceBuilder setStateMachine(FiniteStateMachine finiteStateMachine);

    EndDeviceBuilder setName(String name);

    EndDeviceBuilder setSerialNumber(String serialNumber);
}
