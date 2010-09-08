package com.energyict.protocolimpl.base;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 8-sep-2010
 * Time: 15:32:22
 */
public class UnexpectedEndOfArrayException extends IOException {

    public UnexpectedEndOfArrayException(String s, Exception e) {
        super(s);
        initCause(e);
    }

    public UnexpectedEndOfArrayException(String s, Exception e, byte[] array) {
        super(new StringBuffer().append(s).append(" [").append(ProtocolTools.getHexStringFromBytes(array)).append("]").toString());
        initCause(e);
    }

}
