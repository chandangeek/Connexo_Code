/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class NoDomainIdFoundException extends LocalizedException {

    public NoDomainIdFoundException(Thesaurus thesaurus, Long id) {
        super(thesaurus, MessageSeeds.NO_REALMID_FOUND, id);

        // Set additional parameters here
        //set("domain", domain);
    }
}
