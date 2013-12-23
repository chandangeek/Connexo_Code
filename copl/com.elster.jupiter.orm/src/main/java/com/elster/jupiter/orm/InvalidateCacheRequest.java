package com.elster.jupiter.orm;

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
