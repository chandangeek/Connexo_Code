/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.accessors;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorUserAction;

import java.time.Instant;

/**
 * Created by bvn on 3/15/17.
 */
public class UserActionRecord {
    private SecurityAccessorUserAction userAction;
    private Reference<SecurityAccessorType> keyAccessorType = ValueReference.absent();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    public UserActionRecord() {
    }

    public UserActionRecord(SecurityAccessorType securityAccessorType, SecurityAccessorUserAction userAction) {
        this();
        this.keyAccessorType.set(securityAccessorType);
        this.userAction = userAction;
    }

    public SecurityAccessorUserAction getUserAction() {
        return userAction;
    }

    public SecurityAccessorType getKeyAccessorType() {
        return keyAccessorType.get();
    }
}
