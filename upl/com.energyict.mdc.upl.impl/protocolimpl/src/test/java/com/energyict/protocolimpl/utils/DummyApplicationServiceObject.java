package com.energyict.protocolimpl.utils;

import com.energyict.dlms.aso.*;

/**
 * Copyrights EnergyICT
 * Date: 5/01/12
 * Time: 8:02
 */
public class DummyApplicationServiceObject extends ApplicationServiceObject {

    public DummyApplicationServiceObject() {
        super(new XdlmsAse(), null, new SecurityContext(0, 0, 0, new MockSecurityProvider(), 0), 0);
        getAssociationControlServiceElement().getXdlmsAse().setMaxRecPDUServerSize((short) (1024 * 8));
    }

}
