/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

public class VetoCreateNewConflictForActiveConfigChange extends LocalizedException {

    public VetoCreateNewConflictForActiveConfigChange(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.VETO_CONFIG_CHANGE_ACTIVE_NO_NEW_CONFLICTS_ALLOWED);
    }
}
