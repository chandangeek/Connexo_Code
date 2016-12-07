package com.energyict.mdc.protocol.api.services;

import com.elster.jupiter.license.License;
import com.energyict.mdc.protocol.LicensedProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.util.List;

/**
 * Models the behavior of a component that will provide licensing
 * information about the protocols contained in the same bundle.
 *
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 09:51
 */
public interface LicensedProtocolService {

    /**
     * Finds and returns all available licensed protocols.
     *
     * @param license The License that specifies which protocols are effectively licensed
     * @return a list of all available licensed protocols.
     */
    List<LicensedProtocol> getAllLicensedProtocols(License license);

    LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    boolean isValidJavaClassName(String javaClassName, License license);

}