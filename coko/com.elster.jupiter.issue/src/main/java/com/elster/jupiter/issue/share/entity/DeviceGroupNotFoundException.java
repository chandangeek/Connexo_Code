/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class DeviceGroupNotFoundException extends LocalizedException {

    private static final long serialVersionUID = 1L;

    public DeviceGroupNotFoundException(Thesaurus thesaurus, Object... args) {
        super(thesaurus, MessageSeeds.ISSUE_DEVICE_GROUP_NOT_FOUND, args);
    }
}
