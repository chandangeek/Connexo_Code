/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.device;

public class LocationShortInfo {
    public long id;
    public String name;

    public LocationShortInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public LocationShortInfo(LocationShortInfo info) {
        this.id = info.id;
        this.name = info.name;
    }
}
