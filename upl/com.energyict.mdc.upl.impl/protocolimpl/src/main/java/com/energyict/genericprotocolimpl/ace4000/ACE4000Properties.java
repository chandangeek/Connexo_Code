package com.energyict.genericprotocolimpl.ace4000;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:58:56
 */
public class ACE4000Properties extends AbstractProtocolProperties {

    public static final String TIMEOUT2 = "TimeOut";
    public static final String DEFAULT_TIMEOUT = "30000";

    public ACE4000Properties() {
        this(new Properties());
    }

    public ACE4000Properties(Properties properties) {
        super(properties);
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(TIMEOUT);
        optional.add(RETRIES);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        return required;
    }

    @ProtocolProperty
    public int getTimeout() {
        if (0 == (getIntProperty(TIMEOUT, "0"))) {
            return getIntProperty(TIMEOUT2, DEFAULT_TIMEOUT);
        } else {
            return getIntProperty(TIMEOUT, DEFAULT_TIMEOUT);
        }
    }
}