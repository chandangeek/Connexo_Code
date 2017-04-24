/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TariffSources.java
 *
 * Created on 15 juni 2004, 13:16
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class TariffSources {

    int[] regSource = null;
    ABBA1700MeterType meterType;

    /** Creates a new instance of TariffSources */
    public TariffSources(byte[] data,ABBA1700MeterType meterType) throws IOException {
        this.meterType = meterType;
        regSource = new int[meterType.getNrOfTariffRegisters()];
        parse(data);
    }

    public String toString() {

        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<meterType.getNrOfTariffRegisters();i++) {
            try {
               strBuff.append("Tariff register "+(i+1)+", "+EnergyTypeCode.getDescriptionfromRegSource(getRegSource()[i],true)+"\n");
            }
            catch(NoSuchRegisterException e) {
               strBuff.append("Tariff register "+(i+1)+", no source for "+getRegSource()[i]+"\n");
            }
        }
        return strBuff.toString();
    }

    private void parse(byte[] data) throws IOException {
        for (int i=0;i<meterType.getNrOfTariffRegisters();i++) {
            regSource[i] = ProtocolUtils.getIntLE(data,i,1);
        }
    }

    /**
     * Getter for property regSource.
     * @return Value of property regSource.
     */
    public int[] getRegSource() {
        return this.regSource;
    }


}
