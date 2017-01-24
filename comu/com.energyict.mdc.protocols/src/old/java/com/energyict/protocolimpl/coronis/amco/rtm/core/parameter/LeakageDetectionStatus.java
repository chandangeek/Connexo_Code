package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.amco.rtm.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.LeakageEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 7-apr-2011
 * Time: 16:15:01
 */
public class LeakageDetectionStatus extends AbstractParameter {

    LeakageDetectionStatus(RTM rtm) {
        super(rtm);
    }

    private int status;

    public int getStatus() {
        return status;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.LeakageDetectionStatus;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        status = data[0] & 0xFF;
    }

    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> events = new ArrayList<MeterEvent>();
        EventStatusAndDescription translator = new EventStatusAndDescription();

        if (isHighThresholdOnPortA()) {
            events.add(new MeterEvent(new Date(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, translator.getProtocolCodeForLeakageStatus(LeakageEvent.LEAKAGETYPE_EXTREME, LeakageEvent.A), "Real time extreme leak on port A detected"));
        }
        if (isHighThresholdOnPortB()) {
            events.add(new MeterEvent(new Date(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, translator.getProtocolCodeForLeakageStatus(LeakageEvent.LEAKAGETYPE_EXTREME, LeakageEvent.B), "Real time extreme leak on port B detected"));
        }
        if (isHighThresholdOnPortC()) {
            events.add(new MeterEvent(new Date(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, translator.getProtocolCodeForLeakageStatus(LeakageEvent.LEAKAGETYPE_EXTREME, LeakageEvent.C), "Real time extreme leak on port C detected"));
        }
        if (isHighThresholdOnPortD()) {
            events.add(new MeterEvent(new Date(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, translator.getProtocolCodeForLeakageStatus(LeakageEvent.LEAKAGETYPE_EXTREME, LeakageEvent.D), "Real time extreme leak on port D detected"));
        }
        if (isLowThresholdOnPortA()) {
            events.add(new MeterEvent(new Date(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, translator.getProtocolCodeForLeakageStatus(LeakageEvent.LEAKAGETYPE_RESIDUAL, LeakageEvent.A), "Real time residual leak on port A detected"));
        }
        if (isLowThresholdOnPortB()) {
            events.add(new MeterEvent(new Date(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, translator.getProtocolCodeForLeakageStatus(LeakageEvent.LEAKAGETYPE_RESIDUAL, LeakageEvent.B), "Real time residual leak on port B detected"));
        }
        if (isLowThresholdOnPortC()) {
            events.add(new MeterEvent(new Date(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, translator.getProtocolCodeForLeakageStatus(LeakageEvent.LEAKAGETYPE_RESIDUAL, LeakageEvent.C), "Real time residual leak on port C detected"));
        }
        if (isLowThresholdOnPortD()) {
            events.add(new MeterEvent(new Date(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, translator.getProtocolCodeForLeakageStatus(LeakageEvent.LEAKAGETYPE_RESIDUAL, LeakageEvent.D), "Real time residual leak on port D detected"));
        }
        return events;
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Not allowed to write this parameter");
    }

    public boolean isLowThresholdOnPortA() {
        return (status & 0x01) == 0x01;
    }

    public boolean isHighThresholdOnPortA() {
        return (status & 0x02) == 0x02;
    }

    public boolean isLowThresholdOnPortB() {
        return (status & 0x04) == 0x04;
    }

    public boolean isHighThresholdOnPortB() {
        return (status & 0x08) == 0x08;
    }

    public boolean isLowThresholdOnPortC() {
        return (status & 0x10) == 0x10;
    }

    public boolean isHighThresholdOnPortC() {
        return (status & 0x20) == 0x20;
    }

    public boolean isLowThresholdOnPortD() {
        return (status & 0x40) == 0x40;
    }

    public boolean isHighThresholdOnPortD() {
        return (status & 0x80) == 0x80;
    }
}