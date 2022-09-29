package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.List;


public class HS3400LteRegisterMapper extends LteRegisterMapper {

    private final DlmsSession dlmsSession;

    public HS3400LteRegisterMapper(DlmsSession dlmsSession) {
        super(dlmsSession.getCosemObjectFactory());
        this.dlmsSession = dlmsSession;
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {   //TODO: implement bulk request (after ticket COMMUNICATION-1116 is finished)
        List<LteMapping> lteMappings = getMappings();
        for (LteMapping mapping : lteMappings) {
            if (mapping.getObisCode().equals(obisCode)) {
                return mapping.readRegister(dlmsSession.getCosemObjectFactory());
            }
        }
        return null;
    }

}
