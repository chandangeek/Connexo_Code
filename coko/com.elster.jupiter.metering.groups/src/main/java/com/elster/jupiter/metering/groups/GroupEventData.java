/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups;

public class GroupEventData {

    private long id;
    private Group<?> group;

    public GroupEventData(Group<?> group) {
        this.group = group;
        this.id = group.getId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Group<?> getGroup() {
        return group;
    }
}
