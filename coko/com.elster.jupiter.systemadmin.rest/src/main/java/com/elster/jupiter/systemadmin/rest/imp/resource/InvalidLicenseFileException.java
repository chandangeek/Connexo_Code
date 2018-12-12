/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.nls.LocalizedFieldValidationException;

public class InvalidLicenseFileException extends LocalizedFieldValidationException {

    public InvalidLicenseFileException(MessageSeeds messageSeed, Object... args) {
        super(messageSeed, "licenseFile", args);
    }

}
