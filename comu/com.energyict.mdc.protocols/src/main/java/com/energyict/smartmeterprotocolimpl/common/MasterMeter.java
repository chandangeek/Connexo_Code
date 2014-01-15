package com.energyict.smartmeterprotocolimpl.common;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

/**
 * A <CODE>MasterMeter</CODE> provides general information about an master device (concentrator/gateway/...).
 *
 * <p/>
 * <pre>
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 16:54:06
 * </pre>
 */
public interface MasterMeter {

    /**
     * Search for local slave devices so a general topology can be build up
     */
    void searchForSlaveDevices() throws ConnectionException;

}
