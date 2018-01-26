/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SecurityAccessorTypeUpdater extends SecurityAccessorType.Updater {
    SecurityAccessorTypeUpdater addUserAction(SecurityAccessorUserAction userAction);
    SecurityAccessorTypeUpdater removeUserAction(SecurityAccessorUserAction userAction);
}
