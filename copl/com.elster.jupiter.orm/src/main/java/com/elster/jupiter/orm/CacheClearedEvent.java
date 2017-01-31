/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

/**
 * Created by bbl on 4/01/2016.
 */
public final class CacheClearedEvent {
    private final String componentName;
    private final String tableName;

    public CacheClearedEvent(String componentName, String tableName) {
        this.componentName = componentName;
        this.tableName = tableName;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getTableName() {
        return tableName;
    }
}
