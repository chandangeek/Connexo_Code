/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.license;

import java.util.Optional;

import java.security.SignedObject;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public interface LicenseService {

    String COMPONENTNAME = "LIC";

    // Returns the application names/keys for which there is a license
    List<String> getLicensedApplicationKeys();

    Optional<License> getLicenseForApplication(String applicationKey);

    Optional<Properties> getLicensedValuesForApplication(String applicationKey);

    Optional<String> getLicensedValue(String applicationKey, String licensedKey);

    Set<String> addLicense(SignedObject license) throws Exception;
}
