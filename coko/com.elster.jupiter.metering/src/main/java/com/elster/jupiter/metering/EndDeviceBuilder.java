/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
