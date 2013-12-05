/*
 * EventLogCode.java
 *
 * Created on 18 november 2005, 13:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

/**
 *
 * @author Koen
 */
public class EventLogCode {

    private int code;
    private String event;
    private String argument;
    private int eiCode;

    /** Creates a new instance of EventLogCode */
    public EventLogCode(int code, String event, String argument) {
        this(code, event,argument,MeterEvent.OTHER);
    }
    public EventLogCode(int code, String event, String argument, int eiCode) {
        this.setCode(code);
        this.setEvent(event);
        this.setArgument(argument);
        this.setEiCode(eiCode);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public int getEiCode() {
        return eiCode;
    }

    public void setEiCode(int eiCode) {
        this.eiCode = eiCode;
    }

}
