/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DataQualityOverview<H extends HasId & HasName> {

    H getDevice();

    String getDeviceSerialNumber();

    H getDeviceType();

    H getDeviceConfiguration();

    DeviceDataQualityKpiResults getDataQualityKpiResults();

}
