/*
 * DemandRegistersBasePage.java
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
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class DemandRegistersBasePage extends AbstractBasePage {
    
    private DemandRegister wattsDemand;
    private DemandRegister laggingVARsDemand;
    private DemandRegister voltAmpsVA;
    
    /** Creates a new instance of DemandRegistersBasePage */
    public DemandRegistersBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandRegistersBasePage:\n");
        strBuff.append("   laggingVARsDemand="+getLaggingVARsDemand()+"\n");
        strBuff.append("   voltAmpsVA="+getVoltAmpsVA()+"\n");
        strBuff.append("   wattsDemand="+getWattsDemand()+"\n");
        return strBuff.toString();
    }
    
    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x28DD,142*3);
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        
        TimeZone tz = getBasePagesFactory().getProtocolLink().getTimeZone();
        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);
        
        setWattsDemand(new DemandRegister(data, offset, tz)); offset+=DemandRegister.size();
        setLaggingVARsDemand(new DemandRegister(data, offset, tz)); offset+=DemandRegister.size();
        setVoltAmpsVA(new DemandRegister(data, offset, tz)); offset+=DemandRegister.size();
    }

    public DemandRegister getWattsDemand() {
        return wattsDemand;
    }

    public void setWattsDemand(DemandRegister wattsDemand) {
        this.wattsDemand = wattsDemand;
    }

    public DemandRegister getLaggingVARsDemand() {
        return laggingVARsDemand;
    }

    public void setLaggingVARsDemand(DemandRegister laggingVARsDemand) {
        this.laggingVARsDemand = laggingVARsDemand;
    }

    public DemandRegister getVoltAmpsVA() {
        return voltAmpsVA;
    }

    public void setVoltAmpsVA(DemandRegister voltAmpsVA) {
        this.voltAmpsVA = voltAmpsVA;
    }
    
    
} // public class RealTimeBasePage extends AbstractBasePage
