/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EventRecord.java
 *
 * Created on 23 februari 2006, 15:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class EventsRecord {


    private EndDeviceStdStatus1Bitfield endDeviceStdStatus1Bitfield;
    private EndDeviceStdStatus2Bitfield endDeviceStdStatus2Bitfield;
    private ControlBitfield controlBitfield;


    /** Creates a new instance of EventRecord */
    public EventsRecord(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        endDeviceStdStatus1Bitfield = new EndDeviceStdStatus1Bitfield(data,offset,tableFactory);
        offset+=EndDeviceStdStatus1Bitfield.getSize(tableFactory);
        endDeviceStdStatus2Bitfield = new EndDeviceStdStatus2Bitfield(data,offset,tableFactory);
        offset+=EndDeviceStdStatus2Bitfield.getSize(tableFactory);
        controlBitfield = new ControlBitfield(data,offset,tableFactory);
        offset += ControlBitfield.getSize(tableFactory);

    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventsRecord:\n");
        strBuff.append("   controlBitfield="+getControlBitfield()+"\n");
        strBuff.append("   endDeviceStdStatus1Bitfield="+getEndDeviceStdStatus1Bitfield()+"\n");
        strBuff.append("   endDeviceStdStatus2Bitfield="+getEndDeviceStdStatus2Bitfield()+"\n");
        return strBuff.toString();
    }


    static public int getSize(TableFactory tableFactory) throws IOException {
        return EndDeviceStdStatus1Bitfield.getSize(tableFactory)+EndDeviceStdStatus2Bitfield.getSize(tableFactory)+ControlBitfield.getSize(tableFactory);
    }

    public EndDeviceStdStatus1Bitfield getEndDeviceStdStatus1Bitfield() {
        return endDeviceStdStatus1Bitfield;
    }

    public EndDeviceStdStatus2Bitfield getEndDeviceStdStatus2Bitfield() {
        return endDeviceStdStatus2Bitfield;
    }

    public ControlBitfield getControlBitfield() {
        return controlBitfield;
    }
}
