package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;

import java.util.List;

/**
 * OSGI Service wrapper for {@link LicensedProtocol}s.
 *
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 09:51
 */
public interface LicensedProtocolService {

    /**
     * Finds and returns all available licensed protocols.
     *
     * @return a list of all available licensed protocols.
     */
    public List<LicensedProtocol> getAllLicensedProtocols();

    public LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

}