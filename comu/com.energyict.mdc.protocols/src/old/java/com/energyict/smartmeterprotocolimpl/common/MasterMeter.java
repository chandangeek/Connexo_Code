/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.common;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

public interface MasterMeter {

    /**
     * Search for local slave devices so a general topology can be build up
     */
    void searchForSlaveDevices() throws ConnectionException;

}
