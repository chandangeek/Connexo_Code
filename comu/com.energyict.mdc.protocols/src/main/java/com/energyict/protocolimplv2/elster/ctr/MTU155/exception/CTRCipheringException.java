package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 16:50:52
 */
public class CTRCipheringException extends CTRException {

    public CTRCipheringException(String s, Exception e) {
        super(s, e);
    }

    public CTRCipheringException(String s) {
        super(s);
    }

    public CTRCipheringException(Exception e) {
        super(e);
    }

    public CTRCipheringException() {
    }
}
