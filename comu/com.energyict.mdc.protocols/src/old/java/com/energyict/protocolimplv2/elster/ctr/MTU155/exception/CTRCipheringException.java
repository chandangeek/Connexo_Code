/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

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
