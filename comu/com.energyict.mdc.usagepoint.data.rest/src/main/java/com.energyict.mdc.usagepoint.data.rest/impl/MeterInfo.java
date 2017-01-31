/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

public class MeterInfo {

    public String name;
    public String serialNumber;
    public String state;
    public Long start;
    public Long end;
    public IdWithNameInfo deviceType;
    public boolean active;
}
