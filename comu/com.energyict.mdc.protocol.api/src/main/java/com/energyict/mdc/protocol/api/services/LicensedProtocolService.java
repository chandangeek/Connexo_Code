/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

import com.elster.jupiter.license.License;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;

import java.util.List;

public interface LicensedProtocolService {

    /**
     * Finds and returns all available licensed protocols.
     *
     * @param license The License that specifies which protocols are effectively licensed
     * @return a list of all available licensed protocols.
     */
    public List<LicensedProtocol> getAllLicensedProtocols(License license);

    public LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    public boolean isValidJavaClassName(String javaClassName, License license);

}