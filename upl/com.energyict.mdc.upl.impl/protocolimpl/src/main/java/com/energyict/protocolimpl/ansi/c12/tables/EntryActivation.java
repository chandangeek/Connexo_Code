/*
 * EntryActivation.java
 *
 * Created on 26 oktober 2005, 10:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class EntryActivation {

    
    static public final int SIZE=Event.SIZE+4;
    
    private Event event;

    private int tableIDABitfield; // 16 bit
    // decompositie van tableIDABitfield
    private int procedureNr; // bit 10..0
    private boolean stdVsMfgFlag; // bit 11
    private boolean pendingFlag; // bit 12
    private boolean flag1; // bit 13
    private boolean flag2; // bit 14
    private boolean flag3; // bit 15
    
    
    /** Creates a new instance of EntryActivation */
    public EntryActivation(byte[] data, int dataOrder) throws IOException {
        event = new Event(ProtocolUtils.getSubArray2(data, 0, Event.SIZE));
        tableIDABitfield = C12ParseUtils.getInt(data,Event.SIZE,2,dataOrder);
        procedureNr = tableIDABitfield&0x07FF;
        stdVsMfgFlag = ((tableIDABitfield&0x0800)==0x0800);
        pendingFlag = ((tableIDABitfield&0x1000)==0x1000);
        flag1 = ((tableIDABitfield&0x2000)==0x2000);
        flag2 = ((tableIDABitfield&0x4000)==0x4000);
        flag3 = ((tableIDABitfield&0x8000)==0x8000);
        
    }

    public String toString() {
        return "EntryActivation: event="+getEvent()+", tableIDABitfield="+getTableIDABitfield()+", procedureNr="+getProcedureNr()+
               ", stdVsMfgFlag="+isStdVsMfgFlag()+", pendingFlag="+isPendingFlag()+", flag1="+isFlag1()+", flag2="+isFlag2()+", flag3="+isFlag3();
    }
    
    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getTableIDABitfield() {
        return tableIDABitfield;
    }

    public void setTableIDABitfield(int tableIDABitfield) {
        this.tableIDABitfield = tableIDABitfield;
    }

    public boolean isStdVsMfgFlag() {
        return stdVsMfgFlag;
    }

    public void setStdVsMfgFlag(boolean stdVsMfgFlag) {
        this.stdVsMfgFlag = stdVsMfgFlag;
    }

    public boolean isPendingFlag() {
        return pendingFlag;
    }

    public void setPendingFlag(boolean pendingFlag) {
        this.pendingFlag = pendingFlag;
    }

    public boolean isFlag1() {
        return flag1;
    }

    public void setFlag1(boolean flag1) {
        this.flag1 = flag1;
    }

    public boolean isFlag2() {
        return flag2;
    }

    public void setFlag2(boolean flag2) {
        this.flag2 = flag2;
    }

    public boolean isFlag3() {
        return flag3;
    }

    public void setFlag3(boolean flag3) {
        this.flag3 = flag3;
    }

    public int getProcedureNr() {
        return procedureNr;
    }

    public void setProcedureNr(int procedureNr) {
        this.procedureNr = procedureNr;
    }
    
}
