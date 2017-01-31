/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

/**
 * A {@link ComChannel} that wraps a {@link ServerSerialPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-31 (15:01)
 */
public interface SerialComChannel extends ComChannel {

    public ServerSerialPort getSerialPort();

}