/*
 * RecordTemplate.java
 *
 * Created on 13 september 2006, 13:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SelfReadRegisterAddress {

    private int registerAddress;
    private int registerLength;
    private boolean delayed;
    private int pcxSoftwareUseOnly;

    /** Creates a new instance of RecordTemplate */
    public SelfReadRegisterAddress(byte[] data, int offset) throws IOException {
        setRegisterAddress(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setRegisterLength(ProtocolUtils.getInt(data,offset,1));
        offset++;
        setDelayed((getRegisterLength() & 0x80) == 0x80);
        setRegisterLength(getRegisterLength() & 0x7F);
        setPcxSoftwareUseOnly(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadRegisterAddress:\n");
        strBuff.append("   delayed="+isDelayed()+"\n");
        strBuff.append("   pcxSoftwareUseOnly="+getPcxSoftwareUseOnly()+"\n");
        strBuff.append("   registerAddress=0x"+Integer.toHexString(getRegisterAddress())+"\n");
        strBuff.append("   registerLength="+getRegisterLength()+"\n");
        return strBuff.toString();
    }


    static public int size() {
        return 5;
    }

    public int getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(int registerAddress) {
        this.registerAddress = registerAddress;
    }

    public int getRegisterLength() {
        return registerLength;
    }

    public void setRegisterLength(int registerLength) {
        this.registerLength = registerLength;
    }

    public boolean isDelayed() {
        return delayed;
    }

    public void setDelayed(boolean delayed) {
        this.delayed = delayed;
    }

    public int getPcxSoftwareUseOnly() {
        return pcxSoftwareUseOnly;
    }

    public void setPcxSoftwareUseOnly(int pcxSoftwareUseOnly) {
        this.pcxSoftwareUseOnly = pcxSoftwareUseOnly;
    }

}
