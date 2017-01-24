package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 16:51:39
 */
public class CTRException extends IOException {

    public CTRException() {
        super();
    }

    public CTRException(String s) {
        super(s);
    }

    public CTRException(String s, Exception e) {
        super(s + ": " + e.getMessage());
        initCause(e);
    }

    public CTRException(Exception e) {
        initCause(e);
    }

}
