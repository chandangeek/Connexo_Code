/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.exception;

import com.energyict.protocolimplv2.elster.garnet.frame.RequestFrame;
import com.energyict.protocolimplv2.elster.garnet.structure.NotExecutedErrorResponseStructure;

/**
 * @author sva
 * @since 23/05/2014 - 11:20
 */
public class NotExecutedException extends GarnetException {

    private NotExecutedErrorResponseStructure errorStructure;

    public NotExecutedException(RequestFrame request, NotExecutedErrorResponseStructure errorStructure) {
        super("Command 0x" + Integer.toHexString(request.getFunction().getFunctionCode().getFunctionCode()).toUpperCase() + " not executed: " +
                errorStructure.getNotExecutedError().getErrorCodeInfo());
        this.errorStructure = errorStructure;
    }

    public NotExecutedErrorResponseStructure getErrorStructure() {
        return errorStructure;
    }
}
