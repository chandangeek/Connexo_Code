/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ValidationConfigurationTest extends PurposeValidationResourceTest {


    @Test
    public void noChannelsContainer() throws IOException {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.empty());

        Response response = target(URL + "/validationstatus").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream)response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.validationActive")).isFalse();
    }
}
