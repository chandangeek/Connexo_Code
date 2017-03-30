/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

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
