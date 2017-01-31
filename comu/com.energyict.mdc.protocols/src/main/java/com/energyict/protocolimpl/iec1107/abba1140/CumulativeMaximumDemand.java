/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.mdc.common.Quantity;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author  Koen
 */
public class CumulativeMaximumDemand extends MainRegister {

    int regSource;

    /** Creates a new instance of CumulativeMaximumDemand */
    public CumulativeMaximumDemand(byte[] data) throws IOException {
        super();
        parse(data);
    }

    // TODO ?? energy of demand ??
    private void parse(byte[] data) throws IOException {
        BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,0,8))));
        setRegSource(ProtocolUtils.getIntLE(data,8,1));
        setQuantity(new Quantity(bd,EnergyTypeCode.getUnitFromRegSource(getRegSource(),false)));

    }

    public String toString() {
        return "CMD register: quantity="+getQuantity()+", regSource="+getRegSource();
    }



    /**
     * Getter for property regSource.
     * @return Value of property regSource.
     */
    public int getRegSource() {
        return regSource;
    }

    /**
     * Setter for property regSource.
     * @param regSource New value of property regSource.
     */
    public void setRegSource(int regSource) {
        this.regSource = regSource;
    }

}
