/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

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
