package com.energyict.genericprotocolimpl.elster.ctr.common;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 12:01:06
 */
public class CTRParsingException extends IOException {

    public CTRParsingException(String s) {
        super(s);
    }

    public CTRParsingException(String s, Exception e) {
        super(s + ": " + e.getMessage());
        initCause(e);
    }

    public CTRParsingException(Exception e) {
        initCause(e);
    }

}
