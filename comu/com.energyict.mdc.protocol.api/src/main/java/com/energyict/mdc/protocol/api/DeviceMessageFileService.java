/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import java.util.Optional;

/**
 * Provides services for {@link DeviceMessageFile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-13 (08:43)
 */
public interface DeviceMessageFileService {

    Optional<DeviceMessageFile> findDeviceMessageFile(long id);

}