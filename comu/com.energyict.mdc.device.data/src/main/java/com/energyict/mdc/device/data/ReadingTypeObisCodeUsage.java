/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;

public interface ReadingTypeObisCodeUsage {

    ReadingTypeObisCodeUsage initialize(Device device, ReadingType readingType, ObisCode obisCode);

    ReadingType getReadingType();

    Device getDevice();

    void update();

    ObisCode getObisCode();
}
