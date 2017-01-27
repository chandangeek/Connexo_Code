/*
 * DefaultRegisters.java
 *
 * Created on 7 juli 2004, 12:38
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
public class DefaultRegisters extends AbstractLogicalAddress {


    Quantity activeImport;
    Quantity reactive;
    Quantity apparent;


    /** Creates a new instance of DefaultRegisters */
    public DefaultRegisters(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }

    public String toString() {
        return "DefaultRegisters: activeImport="+getActiveImport()+", reactive="+getReactive()+", apparent="+getApparent();
    }

    public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException {
        // KV TO_DOprotocol doc strates k (x1000)... protocol analysis is different...
        activeImport = new Quantity(BigDecimal.valueOf(ProtocolUtils.getLong(data,0,4)).movePointLeft(getScaler()),Unit.get("kWh"));
        // KV TO_DOprotocol doc strates k (x1000)... protocol analysis is different...
        reactive = new Quantity(BigDecimal.valueOf(ProtocolUtils.getLong(data,4,4)).movePointLeft(getScaler()),Unit.get("kvarh"));
        // KV TO_DOprotocol doc strates k (x1000)... protocol analysis is different...
        apparent = new Quantity(BigDecimal.valueOf(ProtocolUtils.getLong(data,8,4)).movePointLeft(getScaler()),Unit.get("kVAh"));
    }

    public Quantity getTotalValueforObisC(int obisCodeC) throws java.io.IOException {
        if (ObisCode.CODE_C_ACTIVE_IMPORT==obisCodeC)
                return activeImport;
        if (129==obisCodeC)
                return reactive;
        if (9==obisCodeC)
                return apparent;

        throw new NoSuchRegisterException("DefaultRegisters, register wit obis code C field "+obisCodeC+" does not exist!");
    }

    /**
     * Getter for property activeImport.
     * @return Value of property activeImport.
     */
    public com.energyict.cbo.Quantity getActiveImport() {
        return activeImport;
    }
    /**
     * Getter for property reactive.
     * @return Value of property reactive.
     */
    public com.energyict.cbo.Quantity getReactive() {
        return reactive;
    }
    /**
     * Getter for property apparent.
     * @return Value of property apparent.
     */
    public com.energyict.cbo.Quantity getApparent() {
        return apparent;
    }
}
