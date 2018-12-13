/*
 * SessionInfo.java
 *
 * Created on 8 december 2006, 15:49
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SessionInfo {
    
    private boolean sessionActive; // Boolean,
    private int portID; // UNSIGNED8,
    private int sourceAddr; // UNSIGNED8,
    private int passwordLevel; // UNSIGNED8,
    private int timeRemainng; // UNSIGNED16    
    
    /** Creates a new instance of SessionInfo */
    public SessionInfo(byte[] data, int offset) throws IOException {
       setSessionActive(ProtocolUtils.getInt(data,offset++,1) == 1);
       setPortID(ProtocolUtils.getInt(data,offset++,1));
       setSourceAddr(ProtocolUtils.getInt(data,offset++,1));
       setPasswordLevel(ProtocolUtils.getInt(data,offset++,1));
       setTimeRemainng(ProtocolUtils.getInt(data,offset,2));
       offset+=2;
    }
    
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SessionInfo:\n");
        strBuff.append("   passwordLevel="+getPasswordLevel()+"\n");
        strBuff.append("   portID="+getPortID()+"\n");
        strBuff.append("   sessionActive="+isSessionActive()+"\n");
        strBuff.append("   sourceAddr="+getSourceAddr()+"\n");
        strBuff.append("   timeRemainng="+getTimeRemainng()+"\n");
        return strBuff.toString();
    }
    
    static public int size() {
        return 6;
    }

    public boolean isSessionActive() {
        return sessionActive;
    }

    public void setSessionActive(boolean sessionActive) {
        this.sessionActive = sessionActive;
    }

    public int getPortID() {
        return portID;
    }

    public void setPortID(int portID) {
        this.portID = portID;
    }

    public int getSourceAddr() {
        return sourceAddr;
    }

    public void setSourceAddr(int sourceAddr) {
        this.sourceAddr = sourceAddr;
    }

    public int getPasswordLevel() {
        return passwordLevel;
    }

    public void setPasswordLevel(int passwordLevel) {
        this.passwordLevel = passwordLevel;
    }

    public int getTimeRemainng() {
        return timeRemainng;
    }

    public void setTimeRemainng(int timeRemainng) {
        this.timeRemainng = timeRemainng;
    }
            
}
