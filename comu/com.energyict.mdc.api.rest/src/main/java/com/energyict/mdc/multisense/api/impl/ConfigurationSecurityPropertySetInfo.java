package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import java.util.List;

public class ConfigurationSecurityPropertySetInfo extends LinkInfo {

    public String name;
    public LinkInfo authenticationLevel;
    public LinkInfo encryptionLevel;

    public List<PropertyInfo> properties;
}
