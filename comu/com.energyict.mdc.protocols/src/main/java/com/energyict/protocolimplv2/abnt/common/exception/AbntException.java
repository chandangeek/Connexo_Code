/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.exception;

import java.io.IOException;

/**
 * @author sva
 * @since 23/05/2014 - 10:14
 */
public class AbntException extends IOException {

    public AbntException() {
        super();
    }

    public AbntException(String s) {
        super(s);
    }

    public AbntException(String s, Exception e) {
        super(s + ": " + e.getMessage());
        initCause(e);
    }

    public AbntException(Exception e) {
        initCause(e);
    }
}
