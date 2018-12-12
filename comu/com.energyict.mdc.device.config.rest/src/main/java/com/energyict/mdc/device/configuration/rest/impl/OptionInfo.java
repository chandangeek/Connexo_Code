/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

public class OptionInfo extends IdWithNameInfo {
    public OptionInfo() {
        super();
    }

    public OptionInfo(String id, String name) {
        super(id, name);
    }
}
