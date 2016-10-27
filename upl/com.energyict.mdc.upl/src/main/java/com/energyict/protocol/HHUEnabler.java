/*
 * HHUEnabler.java
 *
 * Created on 18 september 2003, 11:20
 */

package com.energyict.protocol;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;

/**
 * @author Koen
 */
public interface HHUEnabler {

    void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException;

    void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException;

    byte[] getHHUDataReadout();

}
