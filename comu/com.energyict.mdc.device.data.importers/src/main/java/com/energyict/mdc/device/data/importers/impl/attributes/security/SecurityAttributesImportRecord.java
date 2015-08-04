package com.energyict.mdc.device.data.importers.impl.attributes.security;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.util.LinkedHashMap;
import java.util.Map;

public class SecurityAttributesImportRecord extends FileImportRecord {

    private String securitySettingsName;
    private Map<String, String> securityAttributes = new LinkedHashMap<>();

    public void setSecuritySettingsName(String securitySettingsName) {
        this.securitySettingsName = securitySettingsName;
    }

    public String getSecuritySettingsName() {
        return securitySettingsName;
    }

    public void addSecurityAttribute(String name, String value) {
        securityAttributes.put(name, value);
    }

    public Map<String, String> getSecurityAttributes() {
        return securityAttributes;
    }
}
