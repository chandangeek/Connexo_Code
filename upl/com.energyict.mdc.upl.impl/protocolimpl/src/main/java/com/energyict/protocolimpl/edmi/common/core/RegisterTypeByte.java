package com.energyict.protocolimpl.edmi.common.core;

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
