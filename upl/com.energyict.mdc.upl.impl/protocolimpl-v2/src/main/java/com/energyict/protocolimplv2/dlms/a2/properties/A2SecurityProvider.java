package com.energyict.protocolimplv2.dlms.a2.properties;

import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimpl.dlms.g3.G3RespondingFrameCounterHandler;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

public class A2SecurityProvider extends NTASecurityProvider {

    /**
     * Create a new instance of NTASecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public A2SecurityProvider(TypedProperties properties, int authenticationLevel, short errorHandling) {
        super(properties, authenticationLevel);
        setRespondingFrameCounterHandling(new G3RespondingFrameCounterHandler(errorHandling));   //Validating that the received FC is higher than the previously received FC.
    }

}
