package com.elster.jupiter.systemadmin.rest.resource;

import com.elster.jupiter.nls.LocalizedFieldValidationException;

public class InvalidLicenseFileException extends LocalizedFieldValidationException {

    public InvalidLicenseFileException() {
        super(MessageSeeds.INVALID_LICENSE_FILE, "licenseFile");
    }

}
