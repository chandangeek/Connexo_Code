package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 12:01:06
 */
public class CTRConfigurationException extends CTRException {

    public CTRConfigurationException() {
    }

    public CTRConfigurationException(Exception e) {
        super(e);
    }

    public CTRConfigurationException(String s) {
        super(s);
    }

    public CTRConfigurationException(String s, Exception e) {
        super(s, e);
    }
}
