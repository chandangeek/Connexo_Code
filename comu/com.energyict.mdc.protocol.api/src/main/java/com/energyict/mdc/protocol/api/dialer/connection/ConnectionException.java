/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * IEC870ConnectionException.java
 *
 * Created on 19 juni 2003, 11:32
 */

package com.energyict.mdc.protocol.api.dialer.connection;

import java.io.IOException;

/**
 * @author Koen
 */
public class ConnectionException extends IOException {

    private short sReason;

    public short getReason() {
        return sReason;
    }

    public ConnectionException(String str) {
        super(str);
        this.sReason = -1;
    }

    public ConnectionException() {
        super();
        this.sReason = -1;

    }

    public ConnectionException(String str, short sReason) {
        super(str);
        this.sReason = sReason;

    }

}