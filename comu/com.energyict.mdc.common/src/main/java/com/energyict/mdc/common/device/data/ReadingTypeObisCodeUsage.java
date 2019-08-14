/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.metering.ReadingType;

import com.energyict.obis.ObisCode;

public interface ReadingTypeObisCodeUsage {

    ReadingTypeObisCodeUsage initialize(Device device, ReadingType readingType, ObisCode obisCode);

    ReadingType getReadingType();

    Device getDevice();

    void update();

    ObisCode getObisCode();
}
