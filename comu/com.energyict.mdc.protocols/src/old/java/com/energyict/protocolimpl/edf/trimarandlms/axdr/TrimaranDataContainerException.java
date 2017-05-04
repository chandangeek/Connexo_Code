/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edf.trimarandlms.axdr;

import java.io.IOException;

public class TrimaranDataContainerException extends IOException {

    public TrimaranDataContainerException(String str) {
        super(str);
    }

    public TrimaranDataContainerException() {
        super();
    }

    public TrimaranDataContainerException(String str, short sReason) {
        super(str);
    }

}