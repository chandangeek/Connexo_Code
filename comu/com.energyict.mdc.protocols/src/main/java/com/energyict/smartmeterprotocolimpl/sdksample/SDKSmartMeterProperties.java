/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.sdksample;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;

import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;

import java.util.ArrayList;
import java.util.List;

public class SDKSmartMeterProperties extends AbstractProtocolProperties {

    public static final String SIMULATE_REAL_COMMUNICATION = "SimulateRealCommunication";

    public static final String DEFAULT_SIMULATE_REAL_COMMUNICATION = "0";

    public static final String READING_QUALITIES = "ReadingQualities";

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(SIMULATE_REAL_COMMUNICATION);
        optional.add(READING_QUALITIES);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        return required;
    }

    @ProtocolProperty
    public boolean isSimulateRealCommunication() {
        return getBooleanProperty(SIMULATE_REAL_COMMUNICATION, DEFAULT_SIMULATE_REAL_COMMUNICATION);
    }

    public String[] getReadingQualities() {
        String readingQualities = getStringValue(READING_QUALITIES, "");
        if (readingQualities.isEmpty()) {
            return new String[0];
        }
        return readingQualities.split(";");
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

    }

}
