package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 21/03/12
 * Time: 15:56
 */
public abstract class G3Mapping {

    private final ObisCode obis;

    protected G3Mapping(ObisCode obis) {
        this.obis = obis;
    }

    public abstract RegisterValue readRegister(AS330D as330D) throws IOException;

    public final ObisCode getObisCode() {
        return obis;
    }
}
