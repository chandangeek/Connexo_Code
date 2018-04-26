/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PurposeValidationResourceTest extends UsagePointDataRestApplicationJerseyTest {

    private static final String USAGEPOINT_NAME = "UP001";
    private static final Long CONTRACT_ID = 13L;
    private static final String URL = "/usagepoints/" + USAGEPOINT_NAME + "/purposes/" + CONTRACT_ID + "/validationrulesets";

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private ChannelsContainer channelsContainer;

    @Before
    public void setup(){
        when(meteringService.findUsagePointByName(USAGEPOINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(usagePoint.getName()).thenReturn(USAGEPOINT_NAME);
        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(metrologyContract.getId()).thenReturn(CONTRACT_ID);
    }


    private ValidationRuleSet mockValidationRuleSet(int id, String name) {
        ValidationRuleSet ruleSet = mock(ValidationRuleSet.class);
        when(ruleSet.getId()).thenReturn(Long.valueOf(id));
        when(ruleSet.getName()).thenReturn(name);
        when(ruleSet.getDescription()).thenReturn("MyDescription");
        when(ruleSet.getRuleSetVersions()).thenReturn(Collections.emptyList());
        when(ruleSet.getVersion()).thenReturn(23L);
        return ruleSet;
    }

    @Test
    public void when_NoLinkedRuleSets_then_EmptyResponse() throws IOException {
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(Collections.emptyList());

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(0);
    }

    @Test
    public void when_BothActiveAndInactiveRuleSets_then_ShowAllSortedLinkedRuleSets() throws IOException {
        ValidationRuleSet ruleSet1 = mockValidationRuleSet(1, "inactive rule set");
        ValidationRuleSet ruleSet2 = mockValidationRuleSet(2, "active rule set");
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(Arrays.asList(ruleSet1, ruleSet2));
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(validationService.activeRuleSets(channelsContainer)).thenReturn(Collections.singletonList(ruleSet2));

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.rulesets[0].name")).isEqualTo("active rule set");
        assertThat(jsonModel.<String>get("$.rulesets[1].name")).isEqualTo("inactive rule set");
        assertThat(jsonModel.<Boolean>get("$.rulesets[0].isActive")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.rulesets[1].isActive")).isFalse();
        assertThat(jsonModel.<Integer>get("$.rulesets[0].numberOfVersions")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.rulesets[1].numberOfVersions")).isEqualTo(0);
    }

    @Test
    public void when_NoChannelsContainerOnMetrologyContract_then_DefaultFalseValidationStatus() throws IOException {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.empty());

        Response response = target(URL + "/validationstatus").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.validationActive")).isFalse();
    }
}
