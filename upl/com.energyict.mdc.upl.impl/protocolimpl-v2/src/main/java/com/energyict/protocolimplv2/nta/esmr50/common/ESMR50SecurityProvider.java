package com.energyict.protocolimplv2.nta.esmr50.common;

import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

public class ESMR50SecurityProvider extends NTASecurityProvider {

    /**
     * Create a new instance of NTASecurityProvider
     *
     * @param properties          - contains the keys for the authentication/encryption
     * @param authenticationLevel
     */
    public ESMR50SecurityProvider(TypedProperties properties, int authenticationLevel) {
        super(properties, authenticationLevel);
    }
}
