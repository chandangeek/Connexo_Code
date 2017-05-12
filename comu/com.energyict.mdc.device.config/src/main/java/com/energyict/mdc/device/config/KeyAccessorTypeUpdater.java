/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.pki.KeyAccessorType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface KeyAccessorTypeUpdater extends KeyAccessorType.Updater {
    KeyAccessorTypeUpdater addUserAction(DeviceSecurityUserAction userAction);
    KeyAccessorTypeUpdater removeUserAction(DeviceSecurityUserAction userAction);
}
