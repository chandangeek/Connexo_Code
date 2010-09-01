package com.energyict.protocolimpl.elster.ctr.objects;

import com.energyict.protocolimpl.elster.ctr.CTRConnection;

/**
 * Copyrights EnergyICT
 * Date: 25-aug-2010
 * Time: 13:26:10
 */
public class CTRObjectFactory {

    private final CTRConnection ctrConnection;

    public CTRObjectFactory(CTRConnection ctrConnection) {
        this.ctrConnection = ctrConnection;
    }

    public CTRConnection getCtrConnection() {
        return ctrConnection;
    }



}
