package com.energyict.mdc.device.data.importers.impl.attributes.connection;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.util.HashMap;
import java.util.Map;

public class ConnectionAttributesImportRecord extends FileImportRecord {

    private String connectionMethodName;
    private Map<String, String> connectionAttributes = new HashMap<>();

    public void setConnectionMethodName(String connectionMethodName) {
        this.connectionMethodName = connectionMethodName;
    }

    public void addConnectionAttribute(String name, String value) {
        connectionAttributes.put(name, value);
    }

    public String getConnectionMethod() {
        return connectionMethodName;
    }

    public Map<String, String> getConnectionAttributes() {
        return connectionAttributes;
    }
}
