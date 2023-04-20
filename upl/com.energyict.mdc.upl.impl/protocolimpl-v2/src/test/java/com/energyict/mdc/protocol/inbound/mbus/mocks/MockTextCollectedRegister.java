/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.mocks;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

public class MockTextCollectedRegister extends MockCollectedRegister {

    public MockTextCollectedRegister(RegisterIdentifier registerIdentifier) {
        super(registerIdentifier);
    }
}
