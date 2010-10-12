package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;

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

    public static final String DEFAULT_TIMEOUT = "2000";
    public static final String DEFAULT_RETRIES = "3";
    public static final String DEFAULT_DELAY_AFTER_ERROR = "100";
    public static final String DEFAULT_FORCED_DELAY = "0";
    public static final String DEFAULT_PASSWORD = "000001";
    public static final String DEFAULT_ADDRESS = "0";

    public static final String DEFAULT_KEYC = "32323232323232323232323232323232";
    public static final String DEFAULT_KEYT = "32323232323232323232323232323232";
    public static final String DEFAULT_KEYF = "32323232323232323232323232323232";

    public MTU155Properties() {
        super(new Properties());
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(TIMEOUT);
        optional.add(RETRIES);
        optional.add(ADDRESS);
        optional.add(PASSWORD);
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
        return getIntPropery(RETRIES, DEFAULT_RETRIES);
    }

    @ProtocolProperty
    public int getTimeout() {
        return getIntPropery(TIMEOUT, DEFAULT_TIMEOUT);
    }

    @ProtocolProperty
    public int getDelayAfterError() {
        return getIntPropery(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR);
    }

    @ProtocolProperty
    public int getForcedDelay() {
        return getIntPropery(FORCED_DELAY, DEFAULT_FORCED_DELAY);
    }

    @ProtocolProperty
    public String getKeyC() {
        return getStringValue(KEYC, DEFAULT_KEYC);
    }

    @ProtocolProperty
    public String getKeyT() {
        return getStringValue(KEYT, DEFAULT_KEYT);
    }

    @ProtocolProperty
    public String getKeyF() {
        return getStringValue(KEYF, DEFAULT_KEYF);
    }

    public byte[] getKeyCBytes() {
        return getByteValue(KEYC, DEFAULT_KEYC);
    }

    public byte[] getKeyTBytes() {
        return getByteValue(KEYT, DEFAULT_KEYT);
    }

    public byte[] getKeyFBytes() {
        return getByteValue(KEYF, DEFAULT_KEYF);
    }

    @ProtocolProperty
    public String getPassword() {
        return getStringValue(PASSWORD, DEFAULT_PASSWORD);
    }

    @ProtocolProperty
    public int getAddress() {
        return getIntPropery(ADDRESS, DEFAULT_ADDRESS);
    }

}
