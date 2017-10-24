/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.pki.SecurityAccessorType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SecurityAccessorTypeUpdater extends SecurityAccessorType.Updater {
    SecurityAccessorTypeUpdater addUserAction(DeviceSecurityUserAction userAction);
    SecurityAccessorTypeUpdater removeUserAction(DeviceSecurityUserAction userAction);
}
