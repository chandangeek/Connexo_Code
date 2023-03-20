/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.List;

public interface MerlinCollectedDataFactory {
    DeviceIdentifier getDeviceIdentifier();

    List<CollectedData> getCollectedData();
}
