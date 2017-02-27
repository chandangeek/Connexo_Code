/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality;

import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UsagePointDataQualityKpi extends DataQualityKpi {

    UsagePointGroup getUsagePointGroup();

    MetrologyPurpose getMetrologyPurpose();

}
