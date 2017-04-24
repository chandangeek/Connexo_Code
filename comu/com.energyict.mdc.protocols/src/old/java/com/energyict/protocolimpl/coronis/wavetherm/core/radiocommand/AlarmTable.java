/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

public class AlarmTable extends AbstractRadioCommand {


    public AlarmTable(WaveTherm waveTherm) {
        super(waveTherm);
    }

    private AlarmEvent[] lowThresholdEvents = new AlarmEvent[5];
    private AlarmEvent[] highThresholdEvents = new AlarmEvent[5];

    public AlarmEvent[] getHighThresholdEvents() {
        return highThresholdEvents;
    }

    public AlarmEvent[] getLowThresholdEvents() {
        return lowThresholdEvents;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.AlarmTable;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        int sensorNumber;
        Date eventDate;
        int duration;
        double integratedValue;
        int offset = 0;

        for (int i = 0; i < 5; i++) {
            sensorNumber = data[offset] & 0xFF;
            offset++;
            eventDate = TimeDateRTCParser.parse(data, offset, 6, getWaveTherm().getTimeZone()).getTime();
            offset += 6;
            duration = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            offset += 2;
            integratedValue = calcValue(ProtocolTools.getUnsignedIntFromBytes(data, offset, 2));
            offset += 2;
            highThresholdEvents[i] = new AlarmEvent(duration, eventDate, integratedValue, sensorNumber);
        }

        for (int i = 0; i < 5; i++) {
            sensorNumber = data[offset] & 0xFF;
            offset++;
            eventDate = TimeDateRTCParser.parse(data, offset, 6, getWaveTherm().getTimeZone()).getTime();
            offset += 6;
            duration = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            offset += 2;
            integratedValue = calcValue(ProtocolTools.getUnsignedIntFromBytes(data, offset, 2));
            offset += 2;
            lowThresholdEvents[i] = new AlarmEvent(duration, eventDate, integratedValue, sensorNumber);
        }
    }

    private double calcValue(int rawValue) {
        double sign = ((rawValue & 0xF800) == 0xF800) ? -1 : 1;  //b15 b14 b12 b11 b10 = 11111 ? ==> indicates a negative value
        return sign * (rawValue & 0x07FF) / 16;
    }

    protected byte[] prepare() throws IOException {
        return new byte[0];         //No extra bytes needed for a detection table request.
    }
}