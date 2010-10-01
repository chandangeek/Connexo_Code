package com.energyict.genericprotocolimpl.elster.ctr.common;

import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 1-okt-2010
 * Time: 8:37:01
 */
public class CTRParserOutOfBoundsException extends CTRParsingException {

    public CTRParserOutOfBoundsException(String message, Exception exception) {
        super(message, exception);
    }

    public CTRParserOutOfBoundsException(String message, Exception exception, byte[] array) {
        super(new StringBuffer().append(message).append(" [").append(ProtocolTools.getHexStringFromBytes(array)).append("]").toString(), exception);
    }

}
