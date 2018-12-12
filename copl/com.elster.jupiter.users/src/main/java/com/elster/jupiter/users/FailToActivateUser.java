/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;


public class FailToActivateUser extends LocalizedException {

    public FailToActivateUser(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.FAIL_ACTIVATE_USER);
    }
}
