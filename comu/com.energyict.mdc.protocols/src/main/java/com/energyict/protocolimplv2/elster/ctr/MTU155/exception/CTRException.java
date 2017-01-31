/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.exception;

import java.io.IOException;

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
