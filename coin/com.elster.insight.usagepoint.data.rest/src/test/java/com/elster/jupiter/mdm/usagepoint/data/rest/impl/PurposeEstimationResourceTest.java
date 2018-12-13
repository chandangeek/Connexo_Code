/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class PurposeEstimationResourceTest extends UsagePointDataRestApplicationJerseyTest{

    private static final Long ID = 13L;
    static final String USAGEPOINT_NAME = "UP001";
    static final String URL = "/usagepoints/" + USAGEPOINT_NAME + "/purposes/" + ID + "/estimationrulesets";

    @Mock
    UsagePoint usagePoint;
    @Mock
    EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    MetrologyContract metrologyContract;
    @Mock
    ChannelsContainer channelsContainer;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(meteringService.findUsagePointByName(USAGEPOINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(usagePoint.getName()).thenReturn(USAGEPOINT_NAME);
        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(metrologyContract.getId()).thenReturn(ID);
        when(channelsContainer.getId()).thenReturn(ID);
    }
}
