/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class FoundUserIsNotActiveException extends LocalizedException {

    public FoundUserIsNotActiveException(Thesaurus thesaurus, String name) {
        super(thesaurus, MessageSeeds.USER_NOT_ACTIVE, name);
    }
}
