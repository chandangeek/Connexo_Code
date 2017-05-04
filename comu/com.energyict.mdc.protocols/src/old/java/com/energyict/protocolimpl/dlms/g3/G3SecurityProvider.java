/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3;

import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40SecurityProvider;

import java.util.Properties;

public class G3SecurityProvider extends Dsmr40SecurityProvider {

    private G3Properties g3Properties;
    private String hlsSecret;

    /**
     * Create a new instance of G3SecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public G3SecurityProvider(Properties properties) {
        super(properties);
        this.g3Properties = new G3Properties(properties);
    }

    /**
     * @return the initial frameCounter
     */
    public long getInitialFrameCounter() {
        if (initialFrameCounter != null) {
            return initialFrameCounter;             //G3 way
        } else {
            return super.getInitialFrameCounter();  //DSMR 4.0 way
    }
    }

    @Override
    public byte[] getHLSSecret() {
        String hexPassword = getProperties().getProperty(Dsmr40Properties.DSMR_40_HEX_PASSWORD);
        if (hexPassword != null) {
            return com.energyict.dlms.DLMSUtils.hexStringToByteArray(hexPassword);
        }
        if (this.hlsSecret == null) {
            this.hlsSecret = g3Properties.getPassword();
        }
        byte[] byteWord = new byte[this.hlsSecret.length()];
        for (int i = 0; i < this.hlsSecret.length(); i++) {
            byteWord[i] = (byte) this.hlsSecret.charAt(i);
        }
        return byteWord;
    }
}