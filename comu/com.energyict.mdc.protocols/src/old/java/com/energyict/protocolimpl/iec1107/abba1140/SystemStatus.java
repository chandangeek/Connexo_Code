/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/** @author  Koen */

public class SystemStatus {

    long value;

    /** Creates a new instance of SystemStatus */
    public SystemStatus(byte[] data) throws IOException {
       value = ProtocolUtils.getIntLE(data,0,4);
    }

    public long getValue() {
        return value;
    }

}
