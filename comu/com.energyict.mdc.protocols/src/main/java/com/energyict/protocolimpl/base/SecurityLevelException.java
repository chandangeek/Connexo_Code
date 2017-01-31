/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SecurityLevelException.java
 *
 * Created on 23 mei 2005, 15:14
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

/**
 *
 * @author  Koen
 */
public class SecurityLevelException extends ConnectionException {

    /** Creates a new instance of SecurityLevelException */
    public SecurityLevelException(String message) {
        super(message);
    }

}
