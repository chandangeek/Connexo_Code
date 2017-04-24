/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.cewe.ceweprometer;

import java.io.IOException;

public class WriteException extends IOException {

    public WriteException(String exceptionInfo) {
        super( exceptionInfo );
    } 
    
    
}
