/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DataQualityOverview {

    String getUsagePointName();

    ServiceKind getServiceKind();

    <H extends HasName & HasId> H getMetrologyConfiguration();

    long getMetrologyPurposeId();

    DataQualityKpiResults getDataQualityKpiResults();

}
