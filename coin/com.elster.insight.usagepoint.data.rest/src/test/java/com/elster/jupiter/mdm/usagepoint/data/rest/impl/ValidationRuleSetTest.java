/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.ValidationRuleSet;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ValidationRuleSetTest extends PurposeValidationResourceTest {


    @Test
    public void noUsagePoint() {
        when(meteringService.findUsagePointByName(USAGEPOINT_NAME)).thenReturn(Optional.empty());

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void noMetrologyConfiguration() {
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void noMetrologyContract() {
        when(metrologyContract.getId()).thenReturn(0L);

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void noLinkedRuleSets() throws IOException {
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(Collections.emptyList());

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(0);

        verifyZeroInteractions(validationService);
    }

    @Test
    public void noActiveRuleSets() throws IOException {
        ValidationRuleSet ruleSet = mockValidationRuleSet(1L, "rule set");
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(ruleSet));
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(validationService.activeRuleSets(channelsContainer)).thenReturn(Collections.emptyList());

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.rulesets[0].isActive")).isFalse();

    }

    @Test
    public void bothActiveAndInactiveRuleSets() throws IOException {
        ValidationRuleSet ruleSet1 = mockValidationRuleSet(1L, "inactive rule set");
        ValidationRuleSet ruleSet2 = mockValidationRuleSet(2L, "active rule set");
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
    public void noChannelsContainer() {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.empty());
        ValidationRuleSet ruleSet = mockValidationRuleSet(1L, "rule set");
        doReturn(Optional.of(ruleSet)).when(validationService).getValidationRuleSet(1L);
        PurposeValidationRuleSetInfo info = new PurposeValidationRuleSetInfo(ruleSet, true);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verifyZeroInteractions(validationService);
    }

    @Test
    public void ruleSetNotFound() throws IOException {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(validationService.getValidationRuleSet(1L)).thenReturn(Optional.empty());
        ValidationRuleSet ruleSet = mockValidationRuleSet(1L, "rule set");
        PurposeValidationRuleSetInfo info = new PurposeValidationRuleSetInfo(ruleSet, true);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void activateRuleSet() throws IOException {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        ValidationRuleSet ruleSet = mockValidationRuleSet(1L, "rule set");
        doReturn(Optional.of(ruleSet)).when(validationService).getValidationRuleSet(1L);
        PurposeValidationRuleSetInfo info = new PurposeValidationRuleSetInfo(ruleSet, true);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).activate(channelsContainer, ruleSet);
    }

    @Test
    public void deactivateRuleSet() throws IOException {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        ValidationRuleSet ruleSet = mockValidationRuleSet(1L, "rule set");
        doReturn(Optional.of(ruleSet)).when(validationService).getValidationRuleSet(1L);
        PurposeValidationRuleSetInfo info = new PurposeValidationRuleSetInfo(ruleSet, false);

        Response response = target(URL + "/1/status").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).deactivate(channelsContainer, ruleSet);
    }
}
