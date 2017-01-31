/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

import com.energyict.protocolimpl.utils.ProtocolTools;

public class CTRParserOutOfBoundsException extends CTRParsingException {

    public CTRParserOutOfBoundsException(String message, Exception exception) {
        super(message, exception);
    }

    public CTRParserOutOfBoundsException(String message, Exception exception, byte[] array) {
        super(new StringBuffer().append(message).append(" [").append(ProtocolTools.getHexStringFromBytes(array)).append("]").toString(), exception);
    }

}
