package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;

import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class RegisterType64BitSignedLong extends AbstractRegisterType {
    
    private long value;
    
    /** Creates a new instance of RegisterType16BitsInt */
    public RegisterType64BitSignedLong(byte[] data) throws ProtocolException {
       setValue(ProtocolUtils.getLong(data,0,8));        
    }

    public BigDecimal getBigDecimal() {
        return new BigDecimal(""+value);
    }   
    
    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }    
}
