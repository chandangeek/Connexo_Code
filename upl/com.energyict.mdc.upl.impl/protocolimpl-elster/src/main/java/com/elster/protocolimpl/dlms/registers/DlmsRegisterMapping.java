package com.elster.protocolimpl.dlms.registers;

import com.energyict.obis.ObisCode;

/**
 * User: heuckeg
 * Date: 24.03.11
 * Time: 14:17
 */
public class DlmsRegisterMapping {

    private com.energyict.obis.ObisCode eiCode;
    private com.elster.dlms.types.basic.ObisCode elCode;

    public DlmsRegisterMapping(com.energyict.obis.ObisCode eiCode, com.elster.dlms.types.basic.ObisCode elCode) {
        this.eiCode = eiCode;
        this.elCode = elCode;
    }

    public ObisCode getEiObisCode() {
        return eiCode;
    }

    public com.elster.dlms.types.basic.ObisCode getElObisCode() {
        return elCode;
    }
}
