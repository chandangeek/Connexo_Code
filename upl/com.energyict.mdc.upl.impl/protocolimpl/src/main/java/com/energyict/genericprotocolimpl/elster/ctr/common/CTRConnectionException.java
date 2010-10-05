package com.energyict.genericprotocolimpl.elster.ctr.common;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 12:01:06
 */
public class CTRConnectionException extends IOException {

    public CTRConnectionException(String s) {
        super(s);
    }

    public CTRConnectionException(String s, Exception e) {
        super(s + ": " + e.getMessage());
        initCause(e);
    }

    public CTRConnectionException(Exception e) {
        initCause(e);
    }

}
