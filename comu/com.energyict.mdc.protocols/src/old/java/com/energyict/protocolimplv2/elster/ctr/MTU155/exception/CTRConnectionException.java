/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

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
