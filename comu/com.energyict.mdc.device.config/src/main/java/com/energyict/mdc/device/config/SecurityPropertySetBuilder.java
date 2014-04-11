package com.energyict.mdc.device.config;

public interface SecurityPropertySetBuilder {

    SecurityPropertySetBuilder authenticationLevel(int level);

    SecurityPropertySetBuilder encryptionLevel(int level);

    SecurityPropertySetBuilder addUserAction(DeviceSecurityUserAction userAction);

    SecurityPropertySet build();
}
