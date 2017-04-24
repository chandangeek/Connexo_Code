/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ProfileType;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.LeakageEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApplicationStatusParser {

    private static final String ORIGIN_BBU = " [origin BBU]";
    private static final String ORIGIN_ODR = " [origin ODR]";
    private WaveFlow waveFlow;
    private boolean bubbleUpOrigin;     //Indicating the origin of the event information: bubble up or on demand read

    public ApplicationStatusParser(WaveFlow waveFlowV2, boolean bubbleUpOrigin) {
        this.waveFlow = waveFlowV2;
        this.bubbleUpOrigin = bubbleUpOrigin;
    }

    public List<MeterEvent> getMeterEvents(boolean usesInitialRFCommand, int applicationStatus, boolean valve) throws IOException {
        EventStatusAndDescription translator = new EventStatusAndDescription(waveFlow.getDeviceType());
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        if (!usesInitialRFCommand && valve) {
            int valveApplicationStatus = waveFlow.getParameterFactory().readValveApplicationStatus();
            if ((valveApplicationStatus & 0x01) == 0x01) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_A, "Tamper (valve wirecut)" + addOriginNotion()));
            }
            if ((valveApplicationStatus & 0x02) == 0x02) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.HARDWARE_ERROR, EventStatusAndDescription.EVENTCODE_VALVE_FAULT, "Valve fault" + addOriginNotion()));
            }
            if ((valveApplicationStatus & 0x04) == 0x04) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, EventStatusAndDescription.EVENTCODE_DEFAULT, "Credit under threshold" + addOriginNotion()));
            }
            if ((valveApplicationStatus & 0x08) == 0x08) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.METER_ALARM, EventStatusAndDescription.EVENTCODE_DEFAULT, "Credit equal to zero" + addOriginNotion()));
            }
        }

        if ((applicationStatus & 0x01) == 0x01) {
            Date eventDate = new Date();
            if (!usesInitialRFCommand) {
                eventDate = waveFlow.getParameterFactory().readBatteryLifeDateEnd();
            }
            meterEvents.add(new MeterEvent(eventDate, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW, "Low battery warning" + addOriginNotion()));
        }
        if ((applicationStatus & 0x02) == 0x02) {
            Date eventDate = new Date();
            if (!usesInitialRFCommand) {
                eventDate = waveFlow.getParameterFactory().readWireCutDetectionDate(0);
            }
            meterEvents.add(new MeterEvent(eventDate, translator.getEventCode(0x02), translator.getProtocolCodeForStatus(0x02), translator.getEventDescription(0x02) + addOriginNotion()));
        }
        if ((applicationStatus & 0x04) == 0x04) {
            Date eventDate = new Date();
            if (!usesInitialRFCommand) {
                eventDate = waveFlow.getParameterFactory().readWireCutDetectionDate(1);
            }
            meterEvents.add(new MeterEvent(eventDate, translator.getEventCode(0x04), translator.getProtocolCodeForStatus(0x04), translator.getEventDescription(0x04) + addOriginNotion()));
        }

        if (usesInitialRFCommand) {
            if ((applicationStatus & 0x08) == 0x08) {
                Date eventDate = new Date();
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.METER_ALARM, translator.getProtocolCodeForLeakage(LeakageEvent.START, LeakageEvent.LEAKAGETYPE_RESIDUAL, LeakageEvent.A), "Leak" + addOriginNotion()));
            }
            if ((applicationStatus & 0x10) == 0x10) {
                Date eventDate = new Date();
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.METER_ALARM, translator.getProtocolCodeForLeakage(LeakageEvent.START, LeakageEvent.LEAKAGETYPE_EXTREME, LeakageEvent.A), "Burst" + addOriginNotion()));
            }
        }

        if ((applicationStatus & 0x20) == 0x20) {
            Date eventDate = new Date();
            if (!usesInitialRFCommand) {
                ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
                if (profileType.isOfType4Iputs()) {
                    eventDate = waveFlow.getParameterFactory().readWireCutDetectionDate(2);
                } else {
                    eventDate = waveFlow.getParameterFactory().readReedFaultDetectionDate(0);
                }
            }
            meterEvents.add(new MeterEvent(eventDate, translator.getEventCode(0x20), translator.getProtocolCodeForStatus(0x20), translator.getEventDescription(0x20) + addOriginNotion()));
        }

        if ((applicationStatus & 0x40) == 0x40) {
            Date eventDate = new Date();
            if (!usesInitialRFCommand) {
                ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
                if (profileType.isOfType4Iputs()) {
                    eventDate = waveFlow.getParameterFactory().readWireCutDetectionDate(3);
                } else {
                    eventDate = waveFlow.getParameterFactory().readReedFaultDetectionDate(1);
                }
            }
            meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_D, "Tamper (wirecut D)" + addOriginNotion()));
        }

        if (usesInitialRFCommand) {
            if ((applicationStatus & 0x80) == 0x80) {
                Date eventDate = new Date();
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.METER_ALARM, translator.getProtocolCodeForSimpleBackflow(0), "Backflow detected" + addOriginNotion()));
            }
        }
        return meterEvents;
    }

    private String addOriginNotion() {
        return bubbleUpOrigin ? ORIGIN_BBU : ORIGIN_ODR;
    }
}