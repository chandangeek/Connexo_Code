/*
 * SelfReadTriggerInfo.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class SelfReadTriggerInfo {
    
    // If TRUE, the corresponding date and time trigger is enabled. A freeze occurs and the programmed action is performed at the specified date and time.
    private boolean enabled; // BOOLEAN,
    
    // This is date and time at which the freeze occurs. There are 12 entries which allow the user to schedule freezes.
    private Date freezeTime; // DATE_AND_TIME,
    
    // When a freeze occurs, the meter executes the freeze action set for this trigger. The actions can be combined. 
    // The action byte is broken down as: Bit0 = Demand Reset Bit1 = Digital State Output Bits 2-7 = Reserved No bits on = No action to be taken.
    private int actionCode; // UNSIGNED8
    
    /** Creates a new instance of Result */
    public SelfReadTriggerInfo(byte[] data,int offset,TimeZone timeZone) throws IOException {
        setEnabled(ProtocolUtils.getInt(data,offset++,1) == 1);
        setFreezeTime(Utils.getCalendarFromDateTime(data,offset, timeZone).getTime());
        offset+=Utils.getDateTimeSize();
        setActionCode(ProtocolUtils.getInt(data,offset++,1));
        
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadTriggerInfo:\n");
        strBuff.append("   actionCode="+getActionCode()+"\n");
        strBuff.append("   enabled="+isEnabled()+"\n");
        strBuff.append("   freezeTime="+getFreezeTime()+"\n");
        return strBuff.toString();
    }
    
    static public int size() {
        return 8;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getFreezeTime() {
        return freezeTime;
    }

    public void setFreezeTime(Date freezeTime) {
        this.freezeTime = freezeTime;
    }

    public int getActionCode() {
        return actionCode;
    }

    public void setActionCode(int actionCode) {
        this.actionCode = actionCode;
    }
    

    
}
