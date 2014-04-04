package com.elster.jupiter.license;

import com.elster.jupiter.license.impl.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Copyrights EnergyICT
 * Date: 28/03/2014
 * Time: 16:41
 */
public final class InvalidLicenseException extends BaseException {

    public InvalidLicenseException() {
        this(MessageSeeds.INVALID_LICENSE);
    }

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
}
