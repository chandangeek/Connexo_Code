/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

public class PassiveCalendarInfo {
    public String name;
    public boolean ghost;

    public PassiveCalendarInfo(String name, boolean ghost) {
        this.name = name;
        this.ghost = ghost;
    }
}
