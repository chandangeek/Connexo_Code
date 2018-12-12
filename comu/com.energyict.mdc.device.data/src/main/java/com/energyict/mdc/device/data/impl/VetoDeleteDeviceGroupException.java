/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class VetoDeleteDeviceGroupException extends LocalizedException {
    public VetoDeleteDeviceGroupException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.GROUP_IS_USED_BY_ANOTHER_GROUP);
    }
}
