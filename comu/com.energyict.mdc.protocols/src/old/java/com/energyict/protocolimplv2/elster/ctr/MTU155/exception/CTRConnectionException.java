package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 12:01:06
 */
public class CTRConnectionException extends CTRException {

    public CTRConnectionException() {
    }

    public CTRConnectionException(Exception e) {
        super(e);
    }

    public CTRConnectionException(String s) {
        super(s);
    }

    public CTRConnectionException(String s, Exception e) {
        super(s, e);
    }
}
