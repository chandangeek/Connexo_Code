/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

import java.io.IOException;

public class FrameSizeException extends IOException {

    private static final long serialVersionUID = 1L;

    FrameSizeException(String msg) {
        super(msg);
    }

}
