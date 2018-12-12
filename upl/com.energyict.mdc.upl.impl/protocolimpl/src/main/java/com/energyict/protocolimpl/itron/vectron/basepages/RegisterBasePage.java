/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.cbo.Quantity;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.itron.protocol.Utils;
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
    private boolean dateRequest=false;
    private BigDecimal value;
    private Date date=null;
    private Quantity quantity;
    private Date selfReadDate;
    
    /** Creates a new instance of RealTimeBasePage */
    public RegisterBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RegisterBasePage:\n");
        strBuff.append("   date="+getDate()+"\n");
        strBuff.append("   dateRequest="+isDateRequest()+"\n");
        strBuff.append("   quantity="+getQuantity()+"\n");
        strBuff.append("   register="+getRegister()+"\n");
        strBuff.append("   selfReadDate="+getSelfReadDate()+"\n");
        strBuff.append("   value="+getValue()+"\n");
        return strBuff.toString();
    }   
    
    protected BasePageDescriptor preparebuild() throws IOException {
        
        //System.out.println("KV_DEBUG> getRegister().getAddress()=0x"+Integer.toHexString(getRegister().getAddress()));
        
        if (!isDateRequest()) {
            return new BasePageDescriptor(getRegister().getAddress(),getRegister().getLength());
        }
        else {
            return new BasePageDescriptor(getRegister().getAddress2(),4);
        }
    }
    
    
    
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        
        if (register.isSelfRead())
            setSelfReadDate(((BasePagesFactory)getBasePagesFactory()).getSelfreadTimestampBasePage(Math.abs(getRegister().getObisCode().getF())).getSelfReadDate());
        
        if (!isDateRequest()) {
            if (getRegister().isFloatingBCD()) {
                setValue(ParseUtils.convertBCDFloatingPoint(data, offset, getRegister().getLength()));
            }
            else {
                setValue(ParseUtils.convertBCDFixedPoint(data, offset, getRegister().getLength(), 32));
            }
            
            setQuantity(new Quantity(getValue(),getRegister().getRegisterConfig().getUnit()));
            setDateRequest(true);
        }
        else {
            TimeZone tz = getBasePagesFactory().getProtocolLink().getTimeZone();
            if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
                tz = ProtocolUtils.getWinterTimeZone(tz);
            
            setDate(Utils.buildTOODate(data,offset, tz, ((BasePagesFactory)getBasePagesFactory()).getRealTimeBasePage().getCalendar()));
            setDateRequest(false);
        }
        
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public boolean isDateRequest() {
        return dateRequest;
    }

    public void setDateRequest(boolean dateRequest) {
        this.dateRequest = dateRequest;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public Date getSelfReadDate() {
        return selfReadDate;
    }

    public void setSelfReadDate(Date selfReadDate) {
        this.selfReadDate = selfReadDate;
    }

        
} // public class RealTimeBasePage extends AbstractBasePage
