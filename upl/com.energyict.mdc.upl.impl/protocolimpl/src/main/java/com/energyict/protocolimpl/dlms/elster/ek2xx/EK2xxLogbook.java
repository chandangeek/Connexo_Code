package com.energyict.protocolimpl.dlms.elster.ek2xx;

import com.energyict.protocol.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class EK2xxLogbook {

    private List<MeterEvent> meterEvents = new ArrayList<>(0);

    private String getEventDescription(int protocolCode) {
        return EK2xxEvents.getEventDescription(protocolCode);
    }

    public void addMeterEvent(MeterEvent meterEvent) {
        this.meterEvents.add(meterEvent);
    }

    public void addMeterEvent(Date eventTime, int eiCode, int protocolCode, String message) {
        MeterEvent meterEvent = new MeterEvent(eventTime, eiCode, protocolCode, message);
        addMeterEvent(meterEvent);
    }

    public void addMeterEvent(Date eventTime, int eiCode, int protocolCode) {
        MeterEvent meterEvent = new MeterEvent(eventTime, eiCode, protocolCode, getEventDescription(protocolCode));
        addMeterEvent(meterEvent);
    }

    void clearLogbook() {
        this.meterEvents.clear();
    }

    public List<MeterEvent> getMeterEvents() {
        return this.meterEvents;
    }

}