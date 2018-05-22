/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EstimationRuleSetsTest extends PurposeEstimationResourceTest {

    private static final long RULESET_ID = 1L;
    private static final String RULESET_NAME = "rule set";

    private EstimationRuleSet ruleSet;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        ruleSet = mockEstimationRuleSet(RULESET_ID, RULESET_NAME);
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(RULESET_ID);

    }

    @Test
    public void noLinkedRuleSets() throws IOException {
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(Collections.emptyList());

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(0);
    }

    @Test
    public void noActiveRuleSets() throws IOException {
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(ruleSet));
        when(estimationService.activeRuleSets(channelsContainer)).thenReturn(Collections.emptyList());

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.rulesets[0].isActive")).isFalse();

    }

    @Test
    public void bothActiveAndInactiveRuleSets() throws IOException {
        EstimationRuleSet ruleSet2 = mockEstimationRuleSet(2L, "active rule set");
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(Arrays.asList(ruleSet, ruleSet2));
        when(estimationService.activeRuleSets(channelsContainer)).thenReturn(Collections.singletonList(ruleSet2));

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.rulesets[0].name")).isEqualTo("active rule set");
        assertThat(jsonModel.<String>get("$.rulesets[1].name")).isEqualTo("rule set");
        assertThat(jsonModel.<Boolean>get("$.rulesets[0].isActive")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.rulesets[1].isActive")).isFalse();
    }

    @Test
    public void ruleSetNotFound() throws IOException {
        when(estimationService.getEstimationRuleSet(RULESET_ID)).thenReturn(Optional.empty());
        PurposeEstimationRuleSetInfo info = new PurposeEstimationRuleSetInfo(ruleSet, true);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void wrongQualityCodeSystem() {
        when(ruleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        PurposeEstimationRuleSetInfo info = new PurposeEstimationRuleSetInfo(ruleSet, true);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }


    @Test
    public void activateRuleSet() throws IOException {
        PurposeEstimationRuleSetInfo info = new PurposeEstimationRuleSetInfo(ruleSet, true);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(estimationService).activate(channelsContainer, ruleSet);
    }


    @Test
    public void deactivateRuleSet() throws IOException {
        PurposeEstimationRuleSetInfo info = new PurposeEstimationRuleSetInfo(ruleSet, false);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(estimationService).deactivate(channelsContainer, ruleSet);
    }


    private EstimationRuleSet mockEstimationRuleSet(long id, String name) {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class);
        when(ruleSet.getId()).thenReturn(id);
        when(ruleSet.getName()).thenReturn(name);
        when(ruleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        return ruleSet;
    }

}
