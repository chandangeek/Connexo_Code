package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:58:55
 */
public class MTU155Properties extends AbstractProtocolProperties {

    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";
    public static final String DELAY_AFTER_ERROR = "DelayAfterError";
    public static final String FORCED_DELAY = "ForcedDelay";
    public static final String KEYC = "KeyC";
    public static final String KEYF = "KeyF";
    public static final String KEYT = "KeyT";
    public static final String PASSWORD = MeterProtocol.PASSWORD;
    public static final String ADDRESS = MeterProtocol.NODEID;
    public static final String CHANNEL_CONFIG = "ChannelConfig";
    public static final String SECURITY_LEVEL = "SecurityLevel";
    public static final String DEBUG = "Debug";

    public static final String DEFAULT_TIMEOUT = "10000";
    public static final String DEFAULT_RETRIES = "3";
    public static final String DEFAULT_DELAY_AFTER_ERROR = "100";
    public static final String DEFAULT_FORCED_DELAY = "0";
    public static final String DEFAULT_KEYC = "1234567890123456";
    public static final String DEFAULT_KEYT = "1234567890123456";
    public static final String DEFAULT_KEYF = "1234567890123456";
    public static final String DEFAULT_PASSWORD = "000001";
    public static final String DEFAULT_ADDRESS = "0";
    public static final String DEFAULT_CHANNEL_CONFIG = "1.0.2:1.2.2:4.0.2:7.0.2:1.1.3:1.3.3:1.F.2:2.0.3:2.1.3:2.3.3:1.A.3";
    public static final String DEFAULT_SECURITY_LEVEL = "1"; // 0 == KeyT, 1 == KeyC, 2 == KeyF
    public static final String DEFAULT_DEBUG = "0";

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
        
    }

    public MTU155Properties() {
        super(new Properties());
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(TIMEOUT);
        optional.add(RETRIES);
        optional.add(DELAY_AFTER_ERROR);
        optional.add(FORCED_DELAY);
        optional.add(PASSWORD);
        optional.add(ADDRESS);
        optional.add(CHANNEL_CONFIG);
        optional.add(SECURITY_LEVEL);
        optional.add(DEBUG);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        required.add(KEYC);
        required.add(KEYF);
        required.add(KEYT);
        return required;
    }

    public void addProperty(String key, String value) {
        getProtocolProperties().setProperty(key, value);
    }

    @ProtocolProperty
    public int getRetries() {
        return getIntProperty(RETRIES, DEFAULT_RETRIES);
    }

    @ProtocolProperty
    public int getTimeout() {
        return getIntProperty(TIMEOUT, DEFAULT_TIMEOUT);
    }

    @ProtocolProperty
    public int getDelayAfterError() {
        return getIntProperty(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR);
    }

    @ProtocolProperty
    public int getForcedDelay() {
        return getIntProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY);
    }

    @ProtocolProperty
    public String getKeyC() {
        return getKeyValue(KEYC, DEFAULT_KEYC);
    }

    @ProtocolProperty
    public String getKeyT() {
        return getKeyValue(KEYT, DEFAULT_KEYT);
    }

    @ProtocolProperty
    public String getKeyF() {
        return getKeyValue(KEYF, DEFAULT_KEYF);
    }

    public byte[] getKeyCBytes() {
        return getByteValue(getKeyC());
    }

    public byte[] getKeyTBytes() {
        return getByteValue(getKeyT());
    }

    public byte[] getKeyFBytes() {
        return getByteValue(getKeyF());
    }

    @ProtocolProperty
    public String getPassword() {
        return getStringValue(PASSWORD, DEFAULT_PASSWORD);
    }

    @ProtocolProperty
    public int getAddress() {
        return getIntProperty(ADDRESS, DEFAULT_ADDRESS);
    }

    @ProtocolProperty
    public MTU155ChannelConfig getChannelConfig() {
        return new MTU155ChannelConfig(getStringValue(CHANNEL_CONFIG, DEFAULT_CHANNEL_CONFIG));
    }

    @ProtocolProperty
    public int getSecurityLevel() {
        return getIntProperty(SECURITY_LEVEL, DEFAULT_SECURITY_LEVEL);
    }

    public String getKeyValue(String propertyName, String defaultValue) {
        String key = getStringValue(propertyName, defaultValue);
        if (key.length() == 16) {
            return ProtocolTools.getHexStringFromBytes(key.getBytes(), "");
        } else if (key.length() == 32) {
            return key;
        } else {
            throw new IllegalArgumentException("Invalid key format! Key should be 16 bytes or 32 bytes long.");
        }
    }

    public boolean isDebug() {
        return getBooleanProperty(DEBUG, DEFAULT_DEBUG);
    }

}
