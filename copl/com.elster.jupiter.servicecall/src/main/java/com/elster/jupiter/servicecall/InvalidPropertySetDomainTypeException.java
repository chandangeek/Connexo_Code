/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class InvalidPropertySetDomainTypeException extends LocalizedException {

    public InvalidPropertySetDomainTypeException(Thesaurus thesaurus, MessageSeed messageSeed, RegisteredCustomPropertySet customPropertySet) {
        super(thesaurus, messageSeed, customPropertySet.getClass().getName());
    }
}
