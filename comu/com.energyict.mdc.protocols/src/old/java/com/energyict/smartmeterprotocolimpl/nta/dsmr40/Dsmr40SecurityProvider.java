/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40;

import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;

import java.util.Properties;
import java.util.Random;

public class Dsmr40SecurityProvider extends NTASecurityProvider {

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public Dsmr40SecurityProvider(Properties properties) {
        super(properties);
        initializeRespondingFrameCounterHandler();
    }

    protected void initializeRespondingFrameCounterHandler() {
        setRespondingFrameCounterHandling(new DSMR40RespondingFrameCounterHandler());
    }

    /**
     * The HLSSecret is the password of the RTU
     *
     * @return the password of the RTU
     */
    @Override
    public byte[] getHLSSecret() {
        String hexPassword = getProperties().getProperty(Dsmr40Properties.DSMR_40_HEX_PASSWORD);
        if (hexPassword != null) {
            return com.energyict.dlms.DLMSUtils.hexStringToByteArray(hexPassword);
        } else {
            return super.getHLSSecret();
        }
    }

    /**
     * @return the initial frameCounter
     */
    public long getInitialFrameCounter() {
        Random generator = new Random();
        return generator.nextLong();
    }
}
