package com.energyict.genericprotocolimpl.elster.ctr.exception;

/**
 * Copyrights EnergyICT
 * Date: 16/02/11
 * Time: 9:14
 */
public class CTRDiscoverException extends CTRException {

    public CTRDiscoverException() {
    }

    public CTRDiscoverException(Exception e) {
        super(e);
    }

    public CTRDiscoverException(String s) {
        super(s);
    }

    public CTRDiscoverException(String s, Exception e) {
        super(s, e);
    }
}
