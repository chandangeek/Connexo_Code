package com.elster.jupiter.license;

import com.elster.jupiter.license.impl.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Copyrights EnergyICT
 * Date: 28/03/2014
 * Time: 16:41
 */
public final class InvalidLicenseException extends BaseException {

    private InvalidLicenseException(MessageSeeds message) {
        super(message);

    }

    public static InvalidLicenseException licenseAlreadyActive() {
        return new InvalidLicenseException(MessageSeeds.ALREADY_ACTIVE);
    }

    public InvalidLicenseException(Throwable cause) {
        super(MessageSeeds.INVALID_LICENSE, cause);
    }

    public static InvalidLicenseException newerLicenseAlreadyExists() {
        return new InvalidLicenseException(MessageSeeds.NEWER_LICENSE_EXISTS);
    }

    public static InvalidLicenseException licenseForOtherApp() {
        return new InvalidLicenseException(MessageSeeds.LICENSE_FOR_OTHER_APP);
    }

    public static InvalidLicenseException invalidLicense() {
        return new InvalidLicenseException(MessageSeeds.INVALID_LICENSE);
    }
}
