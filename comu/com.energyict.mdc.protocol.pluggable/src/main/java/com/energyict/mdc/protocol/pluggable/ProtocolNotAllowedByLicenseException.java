/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to use a protocol class that is not allowed
 * by the License that is currently installed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (10:11)
 */
public class ProtocolNotAllowedByLicenseException extends LocalizedException {

    public ProtocolNotAllowedByLicenseException(Thesaurus thesaurus, String javaClassName) {
        super(thesaurus, MessageSeeds.PROTOCOL_NOT_ALLOWED_BY_LICENSE, javaClassName);
        this.set("javaClassName", javaClassName);
    }

}