/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.cim;

import com.elster.jupiter.metering.events.EndDeviceEventType;

import java.util.Optional;

/**
 * Provides factory services for the easy creation of different {@link EndDeviceEventType}s without knowing the exact mRID.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-23 (14:46)
 */
public interface EndDeviceEventTypeFactory {
    Optional<EndDeviceEventType> getEndDeviceEventType(String mRID);
}