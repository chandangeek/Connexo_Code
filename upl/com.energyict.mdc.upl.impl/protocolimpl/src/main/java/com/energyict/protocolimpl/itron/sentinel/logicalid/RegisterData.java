/*
 * RegisterData.java
 *
 * Created on 7 november 2006, 11:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import java.math.*;
import java.util.*;

/**
 *
 * @author Koen
 */
public class RegisterData {
    
    private LogicalID lid;
    private BigDecimal value;
    private int tariff;
    private Date timestamp;
    
//    public RegisterData() {
//        
//    }
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new RegisterData()));
//    }       
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RegisterData:\n");
        strBuff.append("   lid="+getLid()+"\n");
        strBuff.append("   tariff="+getTariff()+"\n");
        strBuff.append("   timestamp="+getTimestamp()+"\n");
        strBuff.append("   value="+getValue()+"\n");
        return strBuff.toString();
    }
    
    /** Creates a new instance of RegisterData */
    public RegisterData(LogicalID lid,BigDecimal value) {
        this(lid, value, -1);
    }
    public RegisterData(LogicalID lid,BigDecimal value,int tariff) {
        this(lid, value, tariff, null);
    }
    public RegisterData(LogicalID lid,BigDecimal value,int tariff,Date timestamp) {
        this.setLid(lid);
        this.setValue(value);
        this.setTariff(tariff);
        this.setTimestamp(timestamp);
    }

    public LogicalID getLid() {
        return lid;
    }

    public void setLid(LogicalID lid) {
        this.lid = lid;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public int getTariff() {
        return tariff;
    }

    public void setTariff(int tariff) {
        this.tariff = tariff;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    
}
