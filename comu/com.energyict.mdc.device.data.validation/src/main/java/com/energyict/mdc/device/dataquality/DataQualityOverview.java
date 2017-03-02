/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DataQualityOverview {

    String getDeviceName();

    String getDeviceSerialNumber();

    <H extends HasName & HasId> H getDeviceType();

    <H extends HasName & HasId> H getDeviceConfiguration();

    DeviceDataQualityKpiResults getDataQualityKpiResults();

}
