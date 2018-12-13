/*
 * RegisterMultiplierBasePage.java
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
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class RegisterMultiplierBasePage extends AbstractBasePage {
    
    private BigDecimal multiplier;
    
    /** Creates a new instance of RegisterMultiplierBasePage */
    public RegisterMultiplierBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RegisterMultiplierBasePage:\n");
        strBuff.append("   multiplier="+getMultiplier()+"\n");
        return strBuff.toString();
    } 
    
    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x2814,0x4);
    }
    
    protected void parse(byte[] data) throws IOException {
        setMultiplier(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,0,4))));
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

        
} // public class RealTimeBasePage extends AbstractBasePage
