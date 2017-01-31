/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.io.IOException;

/**
 * Implementation of a {@link RegisterProtocol} which does not support the reading of registers
 *
 * @author gna
 * @since 10/04/12 - 15:06
 */
public class DeviceRegisterReadingNotSupported implements RegisterProtocol {

    /**
     * This method is used to request a RegisterInfo object that gives info
     * about the meter's supporting the specific ObisCode. If the ObisCode is
     * not supported, NoSuchRegister is thrown.
     *
     * @param obisCode the ObisCode to request RegisterInfo for
     * @return RegisterInfo about the ObisCode
     * @throws java.io.IOException Thrown in case of an exception
     */
    @Override
    public RegisterInfo translateRegister(final ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
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
