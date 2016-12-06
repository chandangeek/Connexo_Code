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
