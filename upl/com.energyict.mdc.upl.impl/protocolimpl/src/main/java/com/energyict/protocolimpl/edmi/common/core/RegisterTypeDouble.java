package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.protocol.ProtocolException;

import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class RegisterTypeDouble extends AbstractRegisterType {
    
    private double value;
    
    /** Creates a new instance of RegisterTypeByte */
    public RegisterTypeDouble(byte[] data) throws ProtocolException {
                
        // in case of reading TOU registers, if the 'D' is omitted with the R connand, the register is returned as float instead of double
        // See EDMI register manual and command line protocol info page 4-4
        if (data.length == 8) {
            long bits = ((long)data[0] & 0xff) << 56   | 
                        (((long)data[1] & 0xff) << 48) |
                        (((long)data[2] & 0xff) << 40) |
                        (((long)data[3] & 0xff) <<32)  |
                        (((long)data[4] & 0xff) << 24) | 
                        (((long)data[5] & 0xff) << 16) |
                        (((long)data[6] & 0xff) << 8)  |
                        (((long)data[7] & 0xff));

            this.setValue(Double.longBitsToDouble(bits));
        }
        else if (data.length == 4) {
            int bits = (((int)data[0] & 0xff) << 24) | 
                        (((int)data[1] & 0xff) << 16) |
                        (((int)data[2] & 0xff) << 8)  |
                        (((int)data[3] & 0xff));

            this.setValue((double)Float.intBitsToFloat(bits));
        } else {
			throw new ProtocolException("RegisterTypeDouble: data length error, not possible to parse float or double (length="+data.length+")!");
		}
    }

    public BigDecimal getBigDecimal() {
        return new BigDecimal(""+getValue());
    }    
    
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
    
}
