/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DeviceDataQualityKpi extends DataQualityKpi {

    EndDeviceGroup getDeviceGroup();

}
