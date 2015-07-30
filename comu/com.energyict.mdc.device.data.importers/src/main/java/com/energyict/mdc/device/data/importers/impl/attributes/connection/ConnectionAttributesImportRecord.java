package com.energyict.mdc.device.data.importers.impl.attributes.connection;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.util.ArrayList;
import java.util.List;

public class ConnectionAttributesImportRecord extends FileImportRecord {

    private String connectionMethodName;
    private List<String> connectionAttributes = new ArrayList<>();

    public void setConnectionMethodName(String connectionMethodName) {
        this.connectionMethodName = connectionMethodName;
    }

    public void addConnectionAttribute(String connectionAttribute) {
        connectionAttributes.add(connectionAttribute);
    }

    public String getConnectionMethodName() {
        return connectionMethodName;
    }

    public List<String> getConnectionAttributes() {
        return connectionAttributes;
    }
}
