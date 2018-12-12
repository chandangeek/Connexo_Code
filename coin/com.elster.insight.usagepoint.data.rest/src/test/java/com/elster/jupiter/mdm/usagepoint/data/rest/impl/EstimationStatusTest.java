/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.estimation.rest.EstimationStatusInfo;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EstimationStatusTest extends PurposeEstimationResourceTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
    }

    @Test
    public void getStatusInfoOK() throws IOException {
        when(estimationService.isEstimationActive(channelsContainer)).thenReturn(true);

        Response response = target(URL + "/estimationstatus").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.active")).isTrue();
    }

    @Test
    public void activateStatus() {
        EstimationStatusInfo info = new EstimationStatusInfo(true);

        Response response = target(URL + "/estimationstatus").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(estimationService).activateEstimation(channelsContainer);
    }

    @Test
    public void deactivateStatus() {
        EstimationStatusInfo info = new EstimationStatusInfo(false);

        Response response = target(URL + "/estimationstatus").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(estimationService).deactivateEstimation(channelsContainer);
    }


}
