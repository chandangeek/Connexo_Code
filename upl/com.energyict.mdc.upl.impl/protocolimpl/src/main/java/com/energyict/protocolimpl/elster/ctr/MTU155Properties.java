package com.energyict.protocolimpl.elster.ctr;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2010
 * Time: 9:17:11
 */
public class MTU155Properties implements ProtocolProperties {

    private Properties properties = new Properties();

    public List<String> getRequiredKeys() {
        List<String> requiredKeys = new ArrayList<String>();
        return requiredKeys;
    }

    public List<String> getOptionalKeys() {
        List<String> optionalKeys = new ArrayList<String>();
        return optionalKeys;
    }

    public void initProperties(Properties properties) {
        this.properties = properties;
    }

    public String getPassword() {
        return properties.getProperty(PASSWORD, "");
    }

    public String getEncryptionKey() {
        return properties.getProperty(ENCRYPTION_KEY, "");
    }
}
