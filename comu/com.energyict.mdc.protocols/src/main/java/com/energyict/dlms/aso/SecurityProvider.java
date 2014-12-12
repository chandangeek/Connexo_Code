package com.energyict.dlms.aso;

import java.io.IOException;

/**
 * The securityProvider is responsible for providing all possible keys.
 * This is the same as the V2 interface (extends), but it adds methods to change the keys.
 * This interface is used by V1 protocols only.
 *
 * @author gna
 * @change 3th nov. 2009 - Added the getNEWLLSSecret method
 * @change 17th oct. 2021 - Added methods to change the used encryption and authentication keys instantly, this should be called after setting new keys to devices that use them instantly.
 * @change 22th oct. 2012 - Added the getNEWAuthenticationKeys method and getNEWEncryptionKeys method, returning a string array of the original and the wrapped key.
 * @Change 23rd oct. 2013 - This now extends the v2 SecurityProvider, which is used for the new DeviceProtocols
 */
public interface SecurityProvider extends com.energyict.dlms.protocolimplv2.SecurityProvider {

    /**
     * A master key shall be present in each COSEM server logical device configured in the system.
     * This key is used for wrapping global keys. The MasterKey should not be transfered during a session.
     *
     * @throws IOException
     */
    byte[] getMasterKey() throws IOException;

}
