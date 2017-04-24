/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface EstimationBlock {

    Channel getChannel();

    CimChannel getCimChannel();

    ReadingType getReadingType();

    List<? extends Estimatable> estimatables();

    List<ReadingQualityType> getReadingQualityTypes();
}
