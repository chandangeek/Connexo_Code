/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

public class LongIdWithNameInfo {

    public Long id;
    public String name;

    public LongIdWithNameInfo() {
    }

    public LongIdWithNameInfo(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public <H extends HasId & HasName> LongIdWithNameInfo(H hasIdAndName) {
        this.id = hasIdAndName.getId();
        this.name = hasIdAndName.getName();
    }
}
