package com.energyict.mdc.protocol.api.dialer.core;

import com.energyict.mdc.protocol.api.inbound.MeterType;

/**
 * Provides the same functionality as the {@link HHUSignOn} interface, except for:
 * The implementations throw the proper ComServer runtime exceptions, there's no exceptions in the method signatures anymore.
 */
public interface HHUSignOnV2 extends HHUSignOn {

    public void sendBreak();

    public MeterType signOn(String strIdent, String meterID);

    public MeterType signOn(String strIdent, String meterID, int baudrate);

    public MeterType signOn(String strIdent, String meterID, boolean wakeup, int baudrate);

}
