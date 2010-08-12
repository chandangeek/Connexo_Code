package com.energyict.protocolimpl.elster.ctr;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2010
 * Time: 9:17:11
 */
public class MTU155Properties implements ProtocolProperties {

    private static final String DEFAULT_INTERVAL = "900";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_ENCRYPTIONKEY = "";
    private static final String DEFAULT_SERIALNUMBER = "";
    private static final String DEFAULT_NODEADDRESS = "1";
    private static final String DEFAULT_RETRIES = "1";
    private static final String DEFAULT_TIMEOUT = "5000";
    private static final String DEFAULT_FORCED_DELAY = "100";

    private Properties properties = new Properties();

    public List<String> getRequiredKeys() {
        List<String> requiredKeys = new ArrayList<String>();
        return requiredKeys;
    }

    public List<String> getOptionalKeys() {
        List<String> optionalKeys = new ArrayList<String>();
        optionalKeys.add(ENCRYPTIONKEY);
        return optionalKeys;
    }

    public void initProperties(Properties properties) {
        this.properties = properties;
    }

    public String getPassword() {
        return properties.getProperty(PASSWORD, DEFAULT_PASSWORD);
    }

    public String getEncryptionKey() {
        return properties.getProperty(ENCRYPTIONKEY, DEFAULT_ENCRYPTIONKEY);
    }

    public String getSerialNumber() {
        return properties.getProperty(SERIALNUMBER, DEFAULT_SERIALNUMBER);
    }

    public int getrofileInterval() {
        return Integer.valueOf(properties.getProperty(PROFILEINTERVAL, DEFAULT_INTERVAL)).intValue();
    }

    public int getRetries() {
        return Integer.valueOf(properties.getProperty(RETRIES, DEFAULT_RETRIES)).intValue();
    }

    public int getTimeout() {
        return Integer.valueOf(properties.getProperty(TIMEOUT, DEFAULT_TIMEOUT)).intValue();
    }

    public int getForcedDelay() {
        return Integer.valueOf(properties.getProperty(FORCEDDELAY, DEFAULT_FORCED_DELAY)).intValue();
    }

    public int getNodeAddress() {
        return Integer.valueOf(properties.getProperty(NODEADDRESS, DEFAULT_NODEADDRESS)).intValue();
    }

}
