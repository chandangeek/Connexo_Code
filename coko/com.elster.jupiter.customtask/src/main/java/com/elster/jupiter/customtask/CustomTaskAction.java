/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;

public enum CustomTaskAction {
    ADMINISTRATE("action.admin"),
    RUN("action.run"),
    EDIT("action.edit"),
    VIEW("action.view"),
    VIEW_HISTORY("action.viewHistory"),
    VIEW_HISTORY_LOG("action.viewHistoryLog");

    private final String nameKey;

    CustomTaskAction(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getNameKey() {
        return nameKey;
    }

    @Override
    public String toString() {
        return getNameKey();
    }
}
