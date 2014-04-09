package com.elster.jupiter.license;

import com.google.common.base.Optional;

import java.security.SignedObject;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 28/03/2014
 * Time: 16:21
 */
public interface LicenseService {

    String COMPONENTNAME = "LIC";

    // Returns the application names/keys for which there is a license
    List<String> getLicensedApplicationKeys();

    Optional<License> getLicenseForApplication(String applicationKey);

    Optional<Properties> getLicensedValuesForApplication(String applicationKey);

    Optional<String> getLicensedValue(String applicationKey, String licensedKey);

    Set<String> addLicense(SignedObject license) throws Exception;
}
