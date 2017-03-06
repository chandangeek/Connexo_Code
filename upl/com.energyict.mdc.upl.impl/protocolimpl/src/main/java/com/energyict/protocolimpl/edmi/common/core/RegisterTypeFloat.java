package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.protocol.ProtocolException;

import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class RegisterTypeFloat extends AbstractRegisterType {
    
    private float value;
    
    /** Creates a new instance of RegisterTypeByte */
    public RegisterTypeFloat(byte[] data) throws ProtocolException {
        if (data.length == 4) {
        int bits = (((int)data[0] & 0xff) << 24) | 
                   (((int)data[1] & 0xff) << 16) |
                   (((int)data[2] & 0xff) << 8)  |
                   (((int)data[3] & 0xff));
                
        this.setValue(Float.intBitsToFloat(bits));
        } else {
			throw new ProtocolException("RegisterTypeFloat: data length error, not possible to parse fload (length="+data.length+")!");
		}
    }
    
    public BigDecimal getBigDecimal() {
        return new BigDecimal(""+getValue());
    }
    
    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
    
}
