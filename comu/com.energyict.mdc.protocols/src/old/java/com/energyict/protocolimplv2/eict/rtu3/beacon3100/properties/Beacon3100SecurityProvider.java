/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.protocolimpl.dlms.g3.G3RespondingFrameCounterHandler;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

public class Beacon3100SecurityProvider extends NTASecurityProvider {
    /**
     * Create a new instance of NTASecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     * @param authenticationLevel
     */
    public Beacon3100SecurityProvider(TypedProperties properties, int authenticationLevel) {
        super(properties, authenticationLevel);
        setRespondingFrameCounterHandling(new G3RespondingFrameCounterHandler(DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER));
    }

    /**
     * Override, the KEK of the Beacon is stored in property DlmsWanKEK
     */
    @Override
    public byte[] getMasterKey() {
        if (this.masterKey == null) {
            String hex = properties.getTypedProperty(Beacon3100ConfigurationSupport.DLMS_WAN_KEK);
            if (hex == null || hex.isEmpty()) {
                throw DeviceConfigurationException.missingProperty(Beacon3100ConfigurationSupport.DLMS_WAN_KEK);
            }
            this.masterKey = DLMSUtils.hexStringToByteArray(hex);
        }
        return this.masterKey;
    }
}
