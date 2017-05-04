/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.hydreka;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter.ApplicationStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApplicationStatusParser {

    private static final String ORIGIN_BBU = " [origin BBU]";
    private static final String ORIGIN_ODR = " [origin ODR]";
    private boolean bubbleUpOrigin;     //Indicating the origin of the event information: bubble up or on demand read
    private Hydreka waveFlow;

    private String addOriginNotion() {
        return bubbleUpOrigin ? ORIGIN_BBU : ORIGIN_ODR;
    }

    public ApplicationStatusParser(Hydreka waveFlow, boolean bubbleUpOrigin) {
        this.waveFlow = waveFlow;
        this.bubbleUpOrigin = bubbleUpOrigin;
    }

    public List<MeterEvent> getMeterEvents(int status) throws IOException {
        boolean usesInitialRFCommand = waveFlow.usesInitialRFCommand();
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        ApplicationStatus applicationStatus = new ApplicationStatus(waveFlow);
        applicationStatus.setApplicationStatus(status);
        if (applicationStatus.isLeakage()) {
            Date eventTimeStamp = waveFlow.getParameterFactory().readLeakageTimestamp();
            meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.METER_ALARM, EventStatusAndDescription.EVENTCODE_LEAKAGE_RESIDUAL_START_A, "Leakage detected" + addOriginNotion()));
        }
        if (applicationStatus.isModuleEndOfBattery()) {
            Date eventTimeStamp = new Date();
            if (!usesInitialRFCommand) {    //Use current date and time if no extra requests are allowed
                eventTimeStamp = waveFlow.getParameterFactory().readModuleEndOfBatteryTimestamp();
            }
            meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW, "WaveFlow module low battery detected" + addOriginNotion()));
        }
        if (applicationStatus.isProbeEndOfBattery()) {
            Date eventTimeStamp = new Date();
            if (!usesInitialRFCommand) {
                eventTimeStamp = waveFlow.getParameterFactory().readProbeEndOfBatteryTimestamp();
            }
            meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW_PROBE, "Permalog probe low battery detected" + addOriginNotion()));
        }
        if (applicationStatus.isTamper()) {
            Date eventTimeStamp = new Date();
            if (!usesInitialRFCommand) {
                eventTimeStamp = waveFlow.getParameterFactory().readTamperTimestamp();
            }
            meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_A, "Tampering detected" + addOriginNotion()));
        }
        return meterEvents;
    }
}
