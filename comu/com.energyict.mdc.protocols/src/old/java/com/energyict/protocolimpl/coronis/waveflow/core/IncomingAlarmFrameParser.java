/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.util.List;

public interface IncomingAlarmFrameParser {

    /**
     * Parse the incoming alarm frame and return a list of events
     */
    public List<MeterEvent> parseAlarms(byte[] alarmFrame) throws IOException;

}
