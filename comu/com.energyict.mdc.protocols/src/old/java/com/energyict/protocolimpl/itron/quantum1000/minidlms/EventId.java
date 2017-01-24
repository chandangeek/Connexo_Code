/*
 * EventId.java
 *
 * Created on 22 december 2006, 15:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

/**
 *
 * @author Koen
 */
public class EventId {

    private int idLow;
    private int idHigh;
    private String description;
    private int meterEvent;

    /** Creates a new instance of EventId */
    public EventId(int id) {
        this(id,null);
    }
    public EventId(int id,String description) {
        this(id,description, MeterEvent.OTHER);
    }
    public EventId(int id, String description, int meterEvent) {
        this(id, id, description, meterEvent);
    }
    public EventId(int idLow, int idHigh, String description) {
        this(idLow,idHigh,description,MeterEvent.OTHER);
    }
    public EventId(int idLow, int idHigh, String description, int meterEvent) {
        this.setIdLow(idLow);
        this.setIdHigh(idHigh);
        this.setDescription(description);
        this.setMeterEvent(meterEvent);
    }

    public String toString() {
        return "Event: "+getIdLow()+".."+getIdHigh()+", "+getDescription()+", "+getMeterEvent();
    }

    public int getIdLow() {
        return idLow;
    }

    public void setIdLow(int idLow) {
        this.idLow = idLow;
    }

    public int getIdHigh() {
        return idHigh;
    }

    public void setIdHigh(int idHigh) {
        this.idHigh = idHigh;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMeterEvent() {
        return meterEvent;
    }

    public void setMeterEvent(int meterEvent) {
        this.meterEvent = meterEvent;
    }



}
