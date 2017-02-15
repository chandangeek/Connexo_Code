/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.alarmframe;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.GenericHeader;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.ProfileType;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AlarmFrameParser {

    private RTM rtm;
    private int status;
    private int alarmId;
    private Date date;
    private int alarmData;
    private ProfileType profileType;

    public AlarmFrameParser(RTM rtm) {
        this.rtm = rtm;
    }

    public int getStatus() {
        return status;
    }

    public void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        int offset = 0;

        alarmId = data[offset] & 0xFF;   //Received from the RTU+Server, indicates the alarm frame type!
        offset++;

        GenericHeader header = new GenericHeader(rtm);
        header.parse(ProtocolTools.getSubArray(data, offset), rtmFactory);
        profileType = header.getProfileType();

        offset += 23; //Skip the generic header

        status = ProtocolTools.getUnsignedIntFromBytes(data, offset, 3);
        offset += 3;

        TimeZone timeZone = rtm.getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        date = TimeDateRTCParser.parse(data, offset, 7, timeZone).getTime();
        offset += 7;

        alarmData = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
    }

    public List<MeterEvent> getMeterEvents() throws IOException {

        if (profileType.isDigitialPorts()) {
            AlarmFramePulseProfile alarmFrame = new AlarmFramePulseProfile(rtm, alarmData, status, date);
            return alarmFrame.getMeterEvents();
        }
        if (profileType.isEncoderPorts()) {
            AlarmFrameEncoderProfile alarmFrame = new AlarmFrameEncoderProfile(rtm, alarmData, status, date);
            return alarmFrame.getMeterEvents();
        }
        if (profileType.isEvoHop()) {
            AlarmFrameEvoHopProfile alarmFrame = new AlarmFrameEvoHopProfile(rtm, alarmData, status, date);
            return alarmFrame.getMeterEvents();
        }
        if (profileType.isDigitalAndValvePorts()) {
            AlarmFramePulseAndValveProfile alarmFrame = new AlarmFramePulseAndValveProfile(rtm, alarmData, status, date);
            return alarmFrame.getMeterEvents();
        }
        if (profileType.isEncoderAndValvePorts()) {
            AlarmFrameEncoderAndValveProfile alarmFrame = new AlarmFrameEncoderAndValveProfile(rtm, alarmData, status, date);
            return alarmFrame.getMeterEvents();
        } else {
            return new ArrayList<MeterEvent>();
        }
    }

    /**
     * Used in the acknowledgement of the push frame.
     */
    public byte[] getResponse() {
        byte[] ack = new byte[]{alarmId == 0x40 ? (byte) 0xC0 : (byte) 0xC1};
        byte[] statusBytes = ProtocolTools.getBytesFromInt(status, 3);
        return ProtocolTools.concatByteArrays(ack, statusBytes);
    }
}