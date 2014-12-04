package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 12:01:06
 */
public class CTRParsingException extends CTRException {

    public CTRParsingException() {
    }

    public CTRParsingException(Exception e) {
        super(e);
    }

    public CTRParsingException(String s) {
        super(s);
    }

    public CTRParsingException(String s, Exception e) {
        super(s, e);
    }
}
