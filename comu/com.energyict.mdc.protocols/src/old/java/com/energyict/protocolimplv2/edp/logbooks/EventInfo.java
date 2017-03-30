/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp.logbooks;

public class EventInfo {

    private final int eisEventCode;
    private final String description;

    public EventInfo(int eisEventCode, String description) {
        this.eisEventCode = eisEventCode;
        this.description = description;
    }

    public int getEisEventCode() {
        return eisEventCode;
    }

    public String getDescription() {
        return description;
    }
}
