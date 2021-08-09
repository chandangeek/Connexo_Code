/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;

import java.time.Duration;

/**
 * Wraps {@link ComChannel} to add that the ComChannel
 * is actually connected from a {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-19 (14:07)
 */
public interface ComPortRelatedComChannel extends ComChannel, SerialPortComChannel {

    ComPort getComPort();

    void setComPort(ComPort comPort);

    ComChannel getActualComChannel();

    void logRemainingBytes();

    Duration talkTime();

    Counters getSessionCounters();

    Counters getTaskSessionCounters();

    void setTraced(boolean traced);

}