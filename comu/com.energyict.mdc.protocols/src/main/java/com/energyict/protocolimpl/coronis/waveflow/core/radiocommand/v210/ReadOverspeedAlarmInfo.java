/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.PulseWeight;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReadOverspeedAlarmInfo extends AbstractRadioCommand {

    public ReadOverspeedAlarmInfo(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private List<OverSpeedAlarmInfo> alarmInfos = new ArrayList<OverSpeedAlarmInfo>();

    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> result = new ArrayList<MeterEvent>();
        if (alarmInfos.size() == 0) {
            return result;
        }
        PulseWeight pulseWeight = getWaveFlow().getPulseWeight(0);
        String unitDescription = Unit.get(BaseUnit.LITERPERHOUR, pulseWeight.getUnitScaler()).toString();

        for (OverSpeedAlarmInfo alarmInfo : alarmInfos) {
            if (alarmInfo.getFlowRatePerSecond() != 0) {
                result.add(new MeterEvent(alarmInfo.getDate(), MeterEvent.METER_ALARM, EventStatusAndDescription.EVENTCODE_OVERSPEED, "Over speed, max detected flow rate: " + (3600 * alarmInfo.getFlowRatePerSecond() * pulseWeight.getWeight()) + " " + unitDescription));
            }
        }
        return result;
    }

    @Override
    protected void parse(byte[] data) throws IOException {

        int offset = 0;
        int value;
        Date date;

        for (int i = 0; i < 5; i++) {
            value = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            offset += 2;
            if (value == 0) {
                return;
            }

            date = TimeDateRTCParser.parse(data, offset, 6, getWaveFlow().getTimeZone()).getTime();
            offset += 6;

            alarmInfos.add(new OverSpeedAlarmInfo(date, value));
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadOverspeedAlarmInfo;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}