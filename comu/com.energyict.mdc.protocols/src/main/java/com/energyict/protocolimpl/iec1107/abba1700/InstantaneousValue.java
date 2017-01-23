/*
 * InstantaneousValue.java
 *
 * Created on 30 november 2004, 11:15
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author  Koen
 */
public class InstantaneousValue {

    Quantity quantity;


    /** Creates a new instance of InstantaneousValue */
    public InstantaneousValue(byte[] data) throws IOException {
        parse(data);
    }

    private void parse(byte[] data) throws IOException {
        //data = ProtocolUtils.convert2ascii(data);
        if (((int)data[0]&0xFF) == 0xFF)
            throw new IOException("Instantaneous value not available!");
        int format = (int)data[0]&0xFF;
        if ((format & 0x07) == 0x07)
            throw new IOException("Instantaneous value too large!");
        int scale = 4 - (int)(format & 0x07);
        int sign = ((format & 0x80) == 0x80 ? -1 : 1);
        data = ProtocolUtils.getSubArray(data,1);
        BigDecimal bd = BigDecimal.valueOf(sign * ParseUtils.getBCD2Long(data,0,6),scale);
        setQuantity(new Quantity(bd,Unit.get("")));
    }

    /**
     * Getter for property quantity.
     * @return Value of property quantity.
     */
    public Quantity getQuantity() {
        return quantity;
    }

    /**
     * Setter for property quantity.
     * @param quantity New value of property quantity.
     */
    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

}
