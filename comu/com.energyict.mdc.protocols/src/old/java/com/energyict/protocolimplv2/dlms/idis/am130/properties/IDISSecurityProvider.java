/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am130.properties;

import com.energyict.mdc.common.TypedProperties;

import com.energyict.protocolimpl.dlms.g3.G3RespondingFrameCounterHandler;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

public class IDISSecurityProvider extends NTASecurityProvider {

    /**
     * Create a new instance of NTASecurityProvider
     *
     * @param properties          - contains the keys for the authentication/encryption
     */
    public IDISSecurityProvider(TypedProperties properties, int authenticationLevel, short errorHandling) {
        super(properties, authenticationLevel);
        setRespondingFrameCounterHandling(new G3RespondingFrameCounterHandler(errorHandling));   //Validating that the received FC is higher than the previously received FC.
    }
}