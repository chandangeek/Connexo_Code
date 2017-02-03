/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class KYZInputTable extends AbstractBasePage {
    
    private int kYZ1PreviousState;
    private int kYZ1NumberOfPulses;
    private int kYZ2PreviousState;
    private int kYZ2NumberOfPulses;
    private int kYZ3PreviousState;
    private int kYZ3NumberOfPulses;
    
    
    /** Creates a new instance of RealTimeBasePage */
    public KYZInputTable(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("KYZInputTable:\n");
        strBuff.append("   KYZ1NumberOfPulses="+getKYZ1NumberOfPulses()+"\n");
        strBuff.append("   KYZ1PreviousState="+getKYZ1PreviousState()+"\n");
        strBuff.append("   KYZ2NumberOfPulses="+getKYZ2NumberOfPulses()+"\n");
        strBuff.append("   KYZ2PreviousState="+getKYZ2PreviousState()+"\n");
        strBuff.append("   KYZ3NumberOfPulses="+getKYZ3NumberOfPulses()+"\n");
        strBuff.append("   KYZ3PreviousState="+getKYZ3PreviousState()+"\n");
        return strBuff.toString();
    }
    
    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x25C2,0x9);
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setKYZ1PreviousState(ProtocolUtils.getInt(data,offset++,1));
        setKYZ1NumberOfPulses(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setKYZ2PreviousState(ProtocolUtils.getInt(data,offset++,1));
        setKYZ2NumberOfPulses(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setKYZ3PreviousState(ProtocolUtils.getInt(data,offset++,1));
        setKYZ3NumberOfPulses(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        //getBasePagesFactory().getFulcrum().getTimeZone()
    }

    public int getKYZ1PreviousState() {
        return kYZ1PreviousState;
    }

    public void setKYZ1PreviousState(int kYZ1PreviousState) {
        this.kYZ1PreviousState = kYZ1PreviousState;
    }

    public int getKYZ1NumberOfPulses() {
        return kYZ1NumberOfPulses;
    }

    public void setKYZ1NumberOfPulses(int kYZ1NumberOfPulses) {
        this.kYZ1NumberOfPulses = kYZ1NumberOfPulses;
    }

    public int getKYZ2PreviousState() {
        return kYZ2PreviousState;
    }

    public void setKYZ2PreviousState(int kYZ2PreviousState) {
        this.kYZ2PreviousState = kYZ2PreviousState;
    }

    public int getKYZ2NumberOfPulses() {
        return kYZ2NumberOfPulses;
    }

    public void setKYZ2NumberOfPulses(int kYZ2NumberOfPulses) {
        this.kYZ2NumberOfPulses = kYZ2NumberOfPulses;
    }

    public int getKYZ3PreviousState() {
        return kYZ3PreviousState;
    }

    public void setKYZ3PreviousState(int kYZ3PreviousState) {
        this.kYZ3PreviousState = kYZ3PreviousState;
    }

    public int getKYZ3NumberOfPulses() {
        return kYZ3NumberOfPulses;
    }

    public void setKYZ3NumberOfPulses(int kYZ3NumberOfPulses) {
        this.kYZ3NumberOfPulses = kYZ3NumberOfPulses;
    }
    
    
} // public class RealTimeBasePage extends AbstractBasePage
