/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality;

import com.elster.jupiter.metering.ServiceKind;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DataQualityOverview {

    String getUsagePointName();

    ServiceKind getServiceKind();

    UsagePointConfigurationOverview getConfigurationOverview();

    DataQualityKpiResults getDataQualityKpiResults();

    @ProviderType
    interface UsagePointConfigurationOverview {

        long getMetrologyConfigurationId();

        String getMetrologyConfigurationName();

        boolean isEffective();

        long getMetrologyPurposeId();

        long getMetrologyContractId();

    }
}
