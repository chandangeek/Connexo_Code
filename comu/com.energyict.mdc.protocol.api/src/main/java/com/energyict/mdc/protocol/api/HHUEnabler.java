/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HHUEnabler.java
 *
 * Created on 18 september 2003, 11:20
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;

/**
 * @author Koen
 */
public interface HHUEnabler {

    void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException;

    void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException;

    byte[] getHHUDataReadout();

}
