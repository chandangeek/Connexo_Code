/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

class IdWithNameHolder implements HasId, HasName {

    private long id;
    private String name;

    IdWithNameHolder(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}
