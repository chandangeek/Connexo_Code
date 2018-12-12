/*
 * RateRegisters.java
 *
 * Created on 7 juli 2004, 12:35
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class RateRegisters extends AbstractLogicalAddress {

    private static final int NR_OF_IMPORT_REGISTERS=8;
    private static final int NR_OF_EXPORT_REGISTERS=8;
    Quantity[] importValues= new Quantity[NR_OF_IMPORT_REGISTERS];
    Quantity[] exportValues= new Quantity[NR_OF_EXPORT_REGISTERS];

    /** Creates a new instance of RateRegisters */
    public RateRegisters(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Import RateRegisters ");
        for(int i=0;i<NR_OF_IMPORT_REGISTERS;i++) {
            if (i>0) strBuff.append(", ");
            strBuff.append(i+"="+importValues[i].toString());
        }
        strBuff.append("\nExport RateRegisters: ");
        for(int i=0;i<NR_OF_EXPORT_REGISTERS;i++) {
            if (i>0) strBuff.append(", ");
            strBuff.append(i+"="+exportValues[i].toString());
        }
        return strBuff.toString();
    }

    public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException {
        for(int i=0;i<NR_OF_IMPORT_REGISTERS;i++)
            // KV TO_DO protocoldoc states that the value is little endian and the unit is k (x1000)... analysis of protocol is not so...
            importValues[i] = new Quantity(BigDecimal.valueOf(ProtocolUtils.getLong(data,i*4,4)).movePointLeft(getScaler()),Unit.get("kWh"));
        for(int i=0;i<NR_OF_EXPORT_REGISTERS;i++)
            // KV TO_DO protocoldoc states that the value is little endian and the unit is k (x1000)... analysis of protocol is not so...
            exportValues[i] = new Quantity(BigDecimal.valueOf(ProtocolUtils.getLong(data,32+i*4,4)).movePointLeft(getScaler()),Unit.get("kWh"));
    }

    public Quantity getRateRegisterImport(int i) {
        return importValues[i];
    }
    public Quantity getRateRegisterExport(int i) {
        return exportValues[i];
    }

    public Quantity getRateValueforObisCandE(int obisCodeC,int obisCodeE) throws java.io.IOException {
        if ((obisCodeE>=1) && (obisCodeE<=8)) {
            if (obisCodeC == ObisCode.CODE_C_ACTIVE_IMPORT) {
                getRateRegisterImport(obisCodeE-1);
            }
            else if (obisCodeC == ObisCode.CODE_C_ACTIVE_EXPORT) {
                getRateRegisterExport(obisCodeE-1);
            }
        }

        throw new NoSuchRegisterException("TotalRegisters, register wit obis code C.E field "+obisCodeC+"."+obisCodeE+" does not exist!");
    }
}
