/*
 * InstantaneousValue.java
 *
 * Created on 30 november 2004, 11:15
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author  Koen
 */
public class InstantaneousValue {

    Quantity quantity;


    /** Creates a new instance of InstantaneousValue */
    public InstantaneousValue(byte[] data) throws ProtocolException {
        parse(data);
    }

    private void parse(byte[] data) throws ProtocolException {
        //data = ProtocolUtils.convert2ascii(data);
        if (((int)data[0]&0xFF) == 0xFF)
            throw new ProtocolException("Instantaneous value not available!");
        int format = (int)data[0]&0xFF;
        if ((format & 0x07) == 0x07)
            throw new ProtocolException("Instantaneous value too large!");
        int scale = 4 - (format & 0x07);
        int sign = ((format & 0x80) == 0x80 ? -1 : 1);
        data = ProtocolUtils.getSubArray(data,1);
        BigDecimal bd = BigDecimal.valueOf(sign * ParseUtils.getBCD2Long(data,0,6),scale);
        setQuantity(new Quantity(bd,Unit.get("")));
    }

    /**
     * Getter for property quantity.
     * @return Value of property quantity.
     */
    public com.energyict.cbo.Quantity getQuantity() {
        return quantity;
    }

    /**
     * Setter for property quantity.
     * @param quantity New value of property quantity.
     */
    public void setQuantity(com.energyict.cbo.Quantity quantity) {
        this.quantity = quantity;
    }

    static public void main(String[] args) {
        try {
            {
            //byte[] data={0x30,0x34,0x30,0x30,0x30,0x30,0x30,0x32,0x31,0x33,0x32,0x33,0x36,0x33};
            byte[] data={0x00,0x00,0x00,0x02,0x13,0x23,0x63};
            InstantaneousValue iv = new InstantaneousValue(data);
            System.out.println(iv.getQuantity());
            }
//            {
//            byte[] data={0x38,0x31,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x30,0x31,0x32,0x33,0x34};
//            InstantaneousValue iv = new InstantaneousValue(data);
//            System.out.println(iv.getQuantity());
//            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

}
