/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;

@ProviderType
public interface EstimationBlock {

    Channel getChannel();

    CimChannel getCimChannel();

    ReadingType getReadingType();

    List<? extends Estimatable> estimatables();

    /**
     * Since estimation comment feature was introduced reading quality
     * should be created with reading quality type and estimation comment parameters.
     * Estimation comment is an optional field so
     * it it is applicable to use method getReadingQualityTypesWithComments() below in the same way
     *
     * */
    @Deprecated
    List<ReadingQualityType> getReadingQualityTypes();

    Map<ReadingQualityType, ReadingQualityComment> getReadingQualityTypesWithComments();
}
