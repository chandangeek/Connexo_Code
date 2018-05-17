/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EstimationRuleSetsTest extends PurposeEstimationResourceTest {

    private static final long RULESET_ID = 1L;

    @Test
    public void noLinkedRuleSets() throws IOException {
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(Collections.emptyList());

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(0);
    }

    @Test
    public void noActiveRuleSets() throws IOException {
        EstimationRuleSet ruleSet = mockEstimationRuleSet(RULESET_ID, "rule set");
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(ruleSet));
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(estimationService.activeRuleSets(channelsContainer)).thenReturn(Collections.emptyList());

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.rulesets[0].isActive")).isFalse();

    }

    @Test
    public void bothActiveAndInactiveRuleSets() throws IOException {
        EstimationRuleSet ruleSet1 = mockEstimationRuleSet(1L, "inactive rule set");
        EstimationRuleSet ruleSet2 = mockEstimationRuleSet(2L, "active rule set");
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(Arrays.asList(ruleSet1, ruleSet2));
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(estimationService.activeRuleSets(channelsContainer)).thenReturn(Collections.singletonList(ruleSet2));

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.rulesets[0].name")).isEqualTo("active rule set");
        assertThat(jsonModel.<String>get("$.rulesets[1].name")).isEqualTo("inactive rule set");
        assertThat(jsonModel.<Boolean>get("$.rulesets[0].isActive")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.rulesets[1].isActive")).isFalse();
    }

    @Test
    public void ruleSetNotFound() throws IOException {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(estimationService.getEstimationRuleSet(RULESET_ID)).thenReturn(Optional.empty());
        EstimationRuleSet ruleSet = mockEstimationRuleSet(RULESET_ID, "rule set");
        PurposeEstimationRuleSetInfo info = new PurposeEstimationRuleSetInfo(ruleSet, true);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }


    @Test
    public void activateRuleSet() throws IOException {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        EstimationRuleSet ruleSet = mockEstimationRuleSet(RULESET_ID, "rule set");
        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(RULESET_ID);
        PurposeEstimationRuleSetInfo info = new PurposeEstimationRuleSetInfo(ruleSet, true);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(estimationService).activate(channelsContainer, ruleSet);
    }


    @Test
    public void deactivateRuleSet() throws IOException {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        EstimationRuleSet ruleSet = mockEstimationRuleSet(RULESET_ID, "rule set");
        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(RULESET_ID);
        PurposeEstimationRuleSetInfo info = new PurposeEstimationRuleSetInfo(ruleSet, false);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(estimationService).deactivate(channelsContainer, ruleSet);
    }


    private EstimationRuleSet mockEstimationRuleSet(long id, String name) {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class);
        when(ruleSet.getId()).thenReturn(id);
        when(ruleSet.getName()).thenReturn(name);

        return ruleSet;
    }

}
