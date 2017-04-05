/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import com.jayway.jsonpath.JsonModel;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UsagePointOutputValidationResourceTest extends UsagePointDataRestApplicationJerseyTest {

    private static final String USAGEPOINT_NAME = "UP001";
    private static final long CONTRACT_ID = 13L;
    private static final long OUTPUT_ID = 16L;

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private ReadingTypeDeliverable readingTypeDeliverable;

    @Before
    public void before() {
        when(meteringService.findUsagePointByName(USAGEPOINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(usagePoint.getName()).thenReturn(USAGEPOINT_NAME);
        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(metrologyContract.getId()).thenReturn(CONTRACT_ID);
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(readingTypeDeliverable));
        when(readingTypeDeliverable.getId()).thenReturn(OUTPUT_ID);
    }

    @Test
    public void test() {
        String path = "/usagepoints/" + USAGEPOINT_NAME + "/purposes/" + CONTRACT_ID + "/outputs/" + OUTPUT_ID + "/validation";

        // Business method
        String response = target(path).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Object>get("$.validation")).isEqualTo("");
    }
}
