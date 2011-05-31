package com.energyict.genericprotocolimpl.elster.ctr.exception;

import com.energyict.genericprotocolimpl.elster.ctr.structure.NackStructure;

/**
 * Copyrights EnergyICT
 * Date: 27/03/11
 * Time: 14:11
 */
public class CTRNackException extends CTRConnectionException {

    public CTRNackException(NackStructure data) {
        super(data.getReason() != null ? "Received nack: " + data.getReason().getDescription() : "Received nack, but reason was null.");
    }

}
