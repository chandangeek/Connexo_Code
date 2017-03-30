/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.NackStructure;

public class CTRNackException extends CTRConnectionException {

    public CTRNackException(NackStructure data) {
        super(data.getReason() != null ? "Received nack: " + data.getReason().getDescription() : "Received nack, but reason was null.");
    }

}
