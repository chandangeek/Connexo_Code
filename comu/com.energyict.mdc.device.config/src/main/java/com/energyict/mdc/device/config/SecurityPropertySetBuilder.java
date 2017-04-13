/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SecurityPropertySetBuilder {

    SecurityPropertySetBuilder authenticationLevel(int level);

    SecurityPropertySetBuilder encryptionLevel(int level);

    SecurityPropertySetBuilder securitySuite(int suite);

    SecurityPropertySetBuilder requestSecurityLevel(int level);

    SecurityPropertySetBuilder responseSecurityLevel(int level);

    SecurityPropertySet build();
}
