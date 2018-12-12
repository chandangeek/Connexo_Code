/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dualcontrol;

import com.elster.jupiter.orm.MappedByName;

@MappedByName
public enum State {
    INACTIVE,
    PENDING_ACTIVATION,
    ACTIVE,
    PENDING_UPDATE,
    OBSOLETE;

}
