/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TotalRegisters.java
 *
 * Created on 7 juli 2004, 11:24
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class TotalRegisters extends AbstractLogicalAddress {

    private static final int NR_OF_REGISTERS=9;

    String meanings[]={"ActiveImport","ActiveExport","ReactiveImport","ReactiveExport","ReactiveQ1","ReactiveQ2","ReactiveQ3","ReactiveQ4","Apparent"};
    // KV TO_DO protocol doc states k 'x1000' values... analysis of protocol is not so...
    Unit[] units={Unit.get("kWh"),Unit.get("kWh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kVAh")};
    int[] obisCMapping={1,2,3,4,5,6,7,8,9}; // apparent power is configurable. So, manufacturer specific...
    Quantity[] values= new Quantity[NR_OF_REGISTERS];

    /** Creates a new instance of TotalRegisters */
    public TotalRegisters(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TotalRegisters: ");
        for(int i=0;i<NR_OF_REGISTERS;i++) {
            if (i>0) strBuff.append(", ");
            strBuff.append(meanings[i]+"="+values[i].toString());
        }
        return strBuff.toString();
    }

    public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException {
        for(int i=0;i<NR_OF_REGISTERS;i++)
            // KV TO_DO protocoldoc states that the value is little endian... analysis of protocol is not so...
            values[i] = new Quantity(BigDecimal.valueOf(ProtocolUtils.getLong(data,i*4,4)).movePointLeft(getScaler()),units[i]);
    }

    public Quantity getTotalValue(String meaning) throws java.io.IOException {
        for(int i=0;i<NR_OF_REGISTERS;i++) {
            if (meanings[i].compareTo(meaning)==0) {
                return values[i];
            }
        }
        throw new IOException("TotalRegisters, register "+meaning+" does not exist!");
    }

    public Quantity getTotalValue(int i) {
        return values[i];
    }

    public Quantity getTotalValueforObisC(int obisCodeC) throws java.io.IOException {
        for(int i=0;i<NR_OF_REGISTERS;i++) {
            if (obisCMapping[i]==obisCodeC) {
                return values[i];
            }
        }
        throw new NoSuchRegisterException("TotalRegisters, register wit obis code C field "+obisCodeC+" does not exist!");
    }
}
