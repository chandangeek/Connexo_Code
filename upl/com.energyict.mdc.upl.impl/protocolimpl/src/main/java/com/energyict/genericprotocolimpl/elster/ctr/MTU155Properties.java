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
    private static final String KEYC = "KeyC";
    private static final String KEYF = "KeyF";
    private static final String KEYT = "KeyT";

    private static final String DEFAULT_TIMEOUT = "10000";
    private static final String DEFAULT_RETRIES = "3";

    private static final String DEFAULT_KEYC = "00000000000000000000000000000000";
    private static final String DEFAULT_KEYT = "00000000000000000000000000000000";
    private static final String DEFAULT_KEYF = "00000000000000000000000000000000";

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

    public String getKeyC() {
        return getStringValue(KEYC, DEFAULT_KEYC);
    }

    public String getKeyT() {
        return getStringValue(KEYT, DEFAULT_KEYT);
    }

    public String getKeyF() {
        return getStringValue(KEYF, DEFAULT_KEYF);
    }

}
