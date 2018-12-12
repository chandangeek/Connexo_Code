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

import com.energyict.cbo.Quantity;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class RegisterBasePage extends AbstractBasePage {
    
    private Register register;
    
    private BigDecimal value;
    private Date timestamp;
    private Quantity quantity;
    
    private Date fromTimestamp;
    
    /** Creates a new instance of RealTimeBasePage */
    public RegisterBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RegisterBasePage:\n");
        strBuff.append("   fromTimestamp="+getFromTimestamp()+"\n");
        strBuff.append("   quantity="+getQuantity()+"\n");
        strBuff.append("   register="+getRegister()+"\n");
        strBuff.append("   timestamp="+getTimestamp()+"\n");
        strBuff.append("   value="+getValue()+"\n");
        return strBuff.toString();
    }
    
    protected BasePageDescriptor preparebuild() throws IOException {
        
        return new BasePageDescriptor(getRegister().getAddress(),getRegister().getLength());
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        
        TimeZone tz = ((BasePagesFactory)getBasePagesFactory()).getFulcrum().getTimeZone();
        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);
        
        setValue(getRegister().getValue(data).multiply(((BasePagesFactory)getBasePagesFactory()).getFulcrum().getAdjustRegisterMultiplier()));
        
        
        setTimestamp(getRegister().getTimestamp(data, tz));
        setQuantity(new Quantity(getValue(), getRegister().getUnit()));       
        
        if (register.isSelfReadRegister()) { 
            fromTimestamp = ((BasePagesFactory)getBasePagesFactory()).getSelfReadAreasBasePage(register.getSelfReadSet()).getTimeStamp();
        }
        
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public Date getFromTimestamp() {
        return fromTimestamp;
    }

    public void setFromTimestamp(Date fromTimestamp) {
        this.fromTimestamp = fromTimestamp;
    }

        
} // public class RealTimeBasePage extends AbstractBasePage
