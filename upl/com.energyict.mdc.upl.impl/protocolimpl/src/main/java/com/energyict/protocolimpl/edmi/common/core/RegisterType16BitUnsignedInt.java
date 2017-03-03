package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;

import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class RegisterType16BitUnsignedInt extends AbstractRegisterType {
    
    private int value;
    
    /** Creates a new instance of RegisterType16BitsInt */
    public RegisterType16BitUnsignedInt(byte[] data) throws ProtocolException {
       setValue(ProtocolUtils.getInt(data,0,2));
    }
    
    public BigDecimal getBigDecimal() {
        return new BigDecimal(""+value);
    }
    
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }    
}
