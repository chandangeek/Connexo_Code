package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 10/10/12
 * Time: 17:35
 * Author: khe
 */
public interface IncomingAlarmFrameParser {

    /**
     * Parse the incoming alarm frame and return a list of events
     */
    public List<MeterEvent> parseAlarms(byte[] alarmFrame) throws IOException;

}
