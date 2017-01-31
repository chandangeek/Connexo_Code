/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.Optional;

/**
 * A CollectedBreakerStatus identifies the status of the breaker on a device (connected, disconnected, armed)
 *
 * @author sva
 * @since 6/04/2016 - 17:22
 */
public interface CollectedBreakerStatus extends CollectedData {

    /**
     * @return the DeviceIdentifier for which these FirmwareVersions are applicable
     */
    DeviceIdentifier getDeviceIdentifier();

    /**
     * @return the current status of the device breaker. An empty optional can be returned in case the device doesn't support breaker functionality
     * (or in other words: the device is not equipped with a breaker).
     */
    Optional<BreakerStatus> getBreakerStatus();

    void setBreakerStatus(BreakerStatus breakerStatus);

    /**
     * Setter which can be used to inject the {@link DataCollectionConfiguration dataCollectionConfiguration}
     */
    void setDataCollectionConfiguration(DataCollectionConfiguration configuration);

}