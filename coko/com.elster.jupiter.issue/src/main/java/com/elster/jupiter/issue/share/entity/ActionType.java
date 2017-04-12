/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

public enum ActionType {
    ACTION(1L),
    EDIT(2L),
    VIEW(3L),
    REMOVE(4L);

    private final long value;

    private ActionType(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
