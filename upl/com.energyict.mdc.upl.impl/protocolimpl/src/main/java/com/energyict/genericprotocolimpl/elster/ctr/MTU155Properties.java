package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.protocolimpl.base.AbstractProtocolProperties;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:58:55
 */
public class MTU155Properties extends AbstractProtocolProperties {

    private static final String TIMEOUT = "Timeout";
    private static final String RETRIES = "Retries";
    private static final String DELAY_AFTER_ERROR = "DelayAfterError";
    private static final String FORCED_DELAY = "ForcedDelay";
    private static final String KEYC = "KeyC";
    private static final String KEYF = "KeyF";
    private static final String KEYT = "KeyT";

    private static final String DEFAULT_TIMEOUT = "2000";
    private static final String DEFAULT_RETRIES = "3";
    private static final String DEFAULT_DELAY_AFTER_ERROR = "100";
    private static final String DEFAULT_FORCED_DELAY = "0";

    private static final String DEFAULT_KEYC = "32323232323232323232323232323232";
    private static final String DEFAULT_KEYT = "32323232323232323232323232323232";
    private static final String DEFAULT_KEYF = "32323232323232323232323232323232";

    public MTU155Properties() {
        super(new Properties());
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(TIMEOUT);
        optional.add(RETRIES);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        required.add(KEYC);
        required.add(KEYF);
        required.add(KEYT);
        return required;
    }

    public int getRetries() {
        return getIntPropery(RETRIES, DEFAULT_RETRIES);
    }

    public int getTimeout() {
        return getIntPropery(TIMEOUT, DEFAULT_TIMEOUT);
    }

    public int getDelayAfterError() {
        return getIntPropery(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR);
    }

    public int getForcedDelay() {
        return getIntPropery(FORCED_DELAY, DEFAULT_FORCED_DELAY);
    }

    public String getKeyC() {
        return getStringValue(KEYC, DEFAULT_KEYC);
    }

    public String getKeyT() {
        return getStringValue(KEYT, DEFAULT_KEYT);
    }

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

}
