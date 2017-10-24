/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;

import java.time.Instant;

/**
 * Created by bvn on 3/15/17.
 */
class UserActionRecord {
    private DeviceSecurityUserAction userAction;
    private Reference<SecurityAccessorType> keyAccessorType = ValueReference.absent();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    UserActionRecord() {
    }

    UserActionRecord(SecurityAccessorType securityAccessorType, DeviceSecurityUserAction userAction) {
        this();
        this.keyAccessorType.set(securityAccessorType);
        this.userAction = userAction;
    }

    public DeviceSecurityUserAction getUserAction() {
        return userAction;
    }
}
