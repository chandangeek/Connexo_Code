/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Implementation of a {@link RegisterProtocol} which does not support the reading of registers
 *
 * @author gna
 * @since 10/04/12 - 15:06
 */
public class DeviceRegisterReadingNotSupported implements RegisterProtocol {

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        //TODO check if we can get a Problem in this
        throw new UnsupportedException();
    }

    /**
     * Request a RegisterValue object for an ObisCode. If the ObisCode is not
     * supported, NoSuchRegister is thrown.
     *
     * @param obisCode The ObisCode for which to request a RegisterValue
     * @return RegisterValue object for an ObisCode
     * @throws java.io.IOException Thrown in case of an exception
     */
    @Override
    public RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        //TODO check if we can get a Problem in this
        throw new UnsupportedException();
    }
}