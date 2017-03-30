/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

/**
 * Models the exceptional situation where someone is trying to look for an 'unknown' searchdomain ...
 */
public class InvalidSearchDomain extends LocalizedException {

    public InvalidSearchDomain(Thesaurus thesaurus, String searchDomain) {
        super(thesaurus, MessageSeeds.INVALID_SEARCH_DOMAIN, searchDomain);
        this.set("searchDomain", searchDomain);
    }
}
