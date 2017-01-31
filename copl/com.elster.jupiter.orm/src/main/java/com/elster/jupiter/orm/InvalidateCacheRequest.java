/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

/*
 * used to signal other application servers that the cache for an object type has been changed
 */
public class InvalidateCacheRequest {

    private final String componentName;
    private final String tableName;


    public InvalidateCacheRequest(String componentName, String tableName) {
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
