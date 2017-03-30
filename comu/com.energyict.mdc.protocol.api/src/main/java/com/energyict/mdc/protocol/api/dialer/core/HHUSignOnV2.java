/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.dialer.core;

import com.energyict.mdc.protocol.api.inbound.MeterType;

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
