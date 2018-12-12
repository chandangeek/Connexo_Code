/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest.impl;

public class EventInfo {
    public long id;
    public String name;
    public long code;

    public EventInfo () {

    }

    public EventInfo(long id, String name, long code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }
}
