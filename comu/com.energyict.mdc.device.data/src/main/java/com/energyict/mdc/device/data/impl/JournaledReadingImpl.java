/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.JournaledRegisterReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.data.JournaledReading;
import com.energyict.mdc.device.data.Register;

public class JournaledReadingImpl extends ReadingImpl implements JournaledReading {
    protected JournaledReadingImpl(ReadingRecord actualReading, Register<?, ?> register, ReadingRecord previousReading) {
        super(actualReading, register, previousReading);
    }

    protected JournaledReadingImpl(ReadingRecord actualReading, DataValidationStatus validationStatus, Register<?, ?> register, ReadingRecord previousReading) {
        super(actualReading, validationStatus, register, previousReading);
    }

    @Override
    public String getUserName() {
        return getActualReading() instanceof JournaledRegisterReadingRecord ? ((JournaledRegisterReadingRecord) getActualReading()).getUserName() : "";
    }
}
