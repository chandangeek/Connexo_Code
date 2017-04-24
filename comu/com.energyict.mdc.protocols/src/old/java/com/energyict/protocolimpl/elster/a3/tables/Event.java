/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RecordTemplate.java
 *
 * Created on 28 oktober 2005, 17:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Event {

    /*
     Identify the events that can trigger a call (alarm call). There is a control flag for
     each status bit (standard and manufacturers) in ST-3. If the flag is set, an alarm
     call will be made when the corresponding status flag is set in ST-3. The meter
     supports 2 event records in ST-94 so that different phone numbers can be
     used for restoration and alarm events.
    */

    private int edStdStatus1; // 2 Control flags for standard status flags in ST-3
    private int edStdStatus2; // 1 = 0x00.
    private byte[] edMfgStatus; // 13 bytes (per ST-3) Control flags for mfg. status flags in ST-3.
    private int originateControl; // 1 byte b0-2: Primary_phone_number Select the ST-93 or MT-93 phone to use for the call. (0-2)
                                                             // b3: unused = 0
                                                             // b4-6: Secondary_phone_number The meter ignores this field and only uses the Primary phone number.
                                                             // b7: USE_WINDOWS If set to '1' the meter will check the originate windows (ST-93/MT-93). If outside a window, the meter waits until the start of the next window. If set to '0', the meter will ignore call windows and attempt to call immediately. Restoration calls will most likely be configured to ignore windows and call immediately.


    public Event(){}

    /** Creates a new instance of SourceDefinitionEntry */
    public Event(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        setEdStdStatus1(C12ParseUtils.getInt(data,offset, 2, dataOrder)); offset+=2;
        setEdStdStatus2(C12ParseUtils.getInt(data,offset++));
        setEdMfgStatus(ProtocolUtils.getSubArray2(data, offset, 13)); offset+=13;
        setOriginateControl(C12ParseUtils.getInt(data,offset++));
    }

    public String toString() {
        return "Event:\n" +
                "   edMfgStatus=" + ProtocolUtils.outputHexString(getEdMfgStatus()) + "\n" +
                "   edStdStatus1=" + getEdStdStatus1() + "\n" +
                "   edStdStatus2=" + getEdStdStatus2() + "\n" +
                "   originateControl=" + getOriginateControl() + "\n";
    }

    public static int getSize(TableFactory tableFactory) {
        return 17;
    }

    public int getEdStdStatus1() {
        return edStdStatus1;
    }

    public void setEdStdStatus1(int edStdStatus1) {
        this.edStdStatus1 = edStdStatus1;
    }

    public int getEdStdStatus2() {
        return edStdStatus2;
    }

    public void setEdStdStatus2(int edStdStatus2) {
        this.edStdStatus2 = edStdStatus2;
    }

    public byte[] getEdMfgStatus() {
        return edMfgStatus;
    }

    public void setEdMfgStatus(byte[] edMfgStatus) {
        this.edMfgStatus = edMfgStatus;
    }

    public int getOriginateControl() {
        return originateControl;
    }

    public void setOriginateControl(int originateControl) {
        this.originateControl = originateControl;
    }




}
