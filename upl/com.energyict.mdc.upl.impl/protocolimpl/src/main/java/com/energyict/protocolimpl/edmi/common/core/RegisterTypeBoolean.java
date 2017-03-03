package com.energyict.protocolimpl.edmi.common.core;

import java.math.BigDecimal;
/**
 *
 * @author koen
 */
public class RegisterTypeBoolean extends AbstractRegisterType {
    
    private boolean value;
    
    /** Creates a new instance of RegisterTypeBoolean */
    public RegisterTypeBoolean(byte[] data) {
        this.setValue(data[0] == 1);
    }
    
    public boolean isValue() {
        return value;
    }

    public BigDecimal getBigDecimal() {
        return new BigDecimal(isValue()?"1":"0");
    }  
    
    public void setValue(boolean value) {
        this.value = value;
    }
    
    public String getString() {
        return ""+isValue();
    }
    
}
