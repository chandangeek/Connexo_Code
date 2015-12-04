/*
 * RegisterTypeByte.java
 *
 * Created on 22 maart 2006, 8:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.core;

import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class RegisterTypeByte extends AbstractRegisterType {
    
    private byte value;
    
    /** Creates a new instance of RegisterTypeByte */
    public RegisterTypeByte(byte[] data) {
        this.setValue(data[0]);
    }
    
    public BigDecimal getBigDecimal() {
        return new BigDecimal(0xFF & getValue());
    }
    
    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }
    
}
