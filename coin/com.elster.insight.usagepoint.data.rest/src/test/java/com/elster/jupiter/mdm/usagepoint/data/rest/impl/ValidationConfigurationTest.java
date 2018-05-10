/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.validation.ValidationEvaluator;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ValidationConfigurationTest extends PurposeValidationResourceTest {


    @Test
    public void noChannelsContainer() throws IOException {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.empty());

        Response response = target(URL + "/validationstatus").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verifyZeroInteractions(validationService);
    }

    @Test
    public void getValidationStatusOK() throws IOException {
        mockValidationStatusInfo();
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));

        Response response = target(URL + "/validationstatus").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.validationActive")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.hasValidation")).isTrue();
    }


    @Test
    public void noMetrologyContract() {
        when(metrologyContract.getId()).thenReturn(0L);

        Response response = target(URL).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verifyZeroInteractions(validationService);
    }

    @Test
    public void activateConfiguration() {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        UsagePointValidationStatusInfo info = new UsagePointValidationStatusInfo();
        info.validationActive = true;

        Response response = target(URL + "/validationstatus").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).activateValidation(channelsContainer);
    }

    @Test
    public void deactivateConfiguration() {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        UsagePointValidationStatusInfo info = new UsagePointValidationStatusInfo();
        info.validationActive = false;

        Response response = target(URL + "/validationstatus").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).deactivateValidation(channelsContainer);
    }


    private void mockValidationStatusInfo() {
        ValidationEvaluator validationEvaluator = mock(ValidationEvaluator.class);
        when(validationEvaluator.isAllDataValidated(channelsContainer)).thenReturn(false);
        when(validationEvaluator.areSuspectsPresent(EnumSet.of(QualityCodeSystem.MDM), channelsContainer)).thenReturn(false);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationService.isValidationActive(channelsContainer)).thenReturn(false);
        when(usagePointDataCompletionService.getLastChecked(effectiveMetrologyConfiguration.getUsagePoint(), metrologyContract.getMetrologyPurpose())).thenReturn(Optional.empty());
        when(channelsContainer.hasData()).thenReturn(true);
    }
}
