/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class InstantaneousRegMultipliers extends AbstractBasePage {
    
    private BigDecimal powerandSquare;
    private BigDecimal volts;
    private BigDecimal amps;
    
    /** Creates a new instance of RealTimeBasePage */
    public InstantaneousRegMultipliers(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("InstantaneousRegMultipliers:\n");
        strBuff.append("   amps="+getAmps()+"\n");
        strBuff.append("   powerandSquare="+getPowerandSquare()+"\n");
        strBuff.append("   volts="+getVolts()+"\n");
        return strBuff.toString();
    }  
    
    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(315,12);
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        BigDecimal bd;
        
        bd = BigDecimal.valueOf((long) ProtocolUtils.getInt(data,offset,3));
        offset+=3;
        setPowerandSquare(bd.add(BigDecimal.valueOf((long)((int)data[offset]&0xff), 2)));
        offset++;
        
        bd = BigDecimal.valueOf((long)ProtocolUtils.getInt(data,offset,3));
        offset+=3;
        setVolts(bd.add(BigDecimal.valueOf((long)((int)data[offset]&0xff), 2)));
        offset++;

        bd = BigDecimal.valueOf((long)ProtocolUtils.getInt(data,offset,3));
        offset+=3;
        setAmps(bd.add(BigDecimal.valueOf((long)((int)data[offset]&0xff), 2)));
        offset++;
        
    }

    public BigDecimal getPowerandSquare() {
        return powerandSquare;
    }

    public void setPowerandSquare(BigDecimal powerandSquare) {
        this.powerandSquare = powerandSquare;
    }

    public BigDecimal getVolts() {
        return volts;
    }

    public void setVolts(BigDecimal volts) {
        this.volts = volts;
    }

    public BigDecimal getAmps() {
        return amps;
    }

    public void setAmps(BigDecimal amps) {
        this.amps = amps;
    }

        
} // public class RealTimeBasePage extends AbstractBasePage
