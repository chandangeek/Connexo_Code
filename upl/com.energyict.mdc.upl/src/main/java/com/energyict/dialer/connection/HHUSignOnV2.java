/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HHUSignOn.java
 *
 * Created on 18 september 2003, 11:21
 */

package com.energyict.dialer.connection;

import com.energyict.protocol.meteridentification.MeterType;

/**
 * Provides the same functionality as the {@link HHUSignOn} interface, except for:
 * The implementations throw the proper ComServer runtime exceptions, there's no exceptions in the method signatures anymore.
 */
public interface HHUSignOnV2 extends HHUSignOn {

    void sendBreak();

    MeterType signOn(String strIdent, String meterID);

    MeterType signOn(String strIdent, String meterID, int baudrate);

    MeterType signOn(String strIdent, String meterID, boolean wakeup, int baudrate);

}