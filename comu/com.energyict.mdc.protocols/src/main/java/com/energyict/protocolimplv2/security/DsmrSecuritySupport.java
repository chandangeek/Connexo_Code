/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class DsmrSecuritySupport extends DlmsSecuritySupport {

    @Inject
    public DsmrSecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(
                new NoAuthentication(),
                new LowLevelAuthentication(),
                new Md5Authentication(),
                new Sha1Authentication(),
                new GmacAuthentication());
    }

}