package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 27/02/12
 * Time: 13:22
 */
public class AS300DRegisters {

    private final DlmsSession session;

    public AS300DRegisters(DlmsSession session) {
        this.session = session;
    }

    public static RegisterInfo translateRegister(ObisCode obisCode) {
        return obisCode == null ? null : new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        CosemObjectFactory cof = session.getCosemObjectFactory();
        Register register = cof.getRegister(obisCode);
        Quantity value = register.getQuantityValue();
        return new RegisterValue(obisCode, value);
    }

}
