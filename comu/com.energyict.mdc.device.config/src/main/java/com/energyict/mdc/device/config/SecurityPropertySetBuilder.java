package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SecurityPropertySetBuilder {

    SecurityPropertySetBuilder authenticationLevel(int level);

    SecurityPropertySetBuilder encryptionLevel(int level);

    SecurityPropertySetBuilder addUserAction(DeviceSecurityUserAction userAction);

    SecurityPropertySet build();
}
