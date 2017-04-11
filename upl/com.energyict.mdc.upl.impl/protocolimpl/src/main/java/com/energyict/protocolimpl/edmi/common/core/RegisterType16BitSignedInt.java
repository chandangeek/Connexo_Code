package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;

import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class RegisterType16BitSignedInt extends AbstractRegisterType {
    
    private int value;
    
    /** Creates a new instance of RegisterType16BitsInt */
    public RegisterType16BitSignedInt(byte[] data) throws ProtocolException {
       setValue(ProtocolUtils.getShort(data,0));
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
