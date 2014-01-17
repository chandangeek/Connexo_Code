/*
 * RecordTemplate.java
 *
 * Created on 13 september 2006, 13:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ChannelProgram {

    private int registerNumber;
    private int multiplier;

    /** Creates a new instance of ChannelProgram */
    public ChannelProgram(byte[] data, int offset) throws IOException {
        setRegisterNumber((int)data[offset++] & 0xFF);
        setMultiplier(ProtocolUtils.getInt(data,offset,2));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ChannelProgram:\n");
        strBuff.append("   multiplier="+getMultiplier()+"\n");
        strBuff.append("   registerNumber="+getRegisterNumber()+"\n");
        return strBuff.toString();
    }


    static public int size() {
        return 3;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(int registerNumber) {
        this.registerNumber = registerNumber;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

}
