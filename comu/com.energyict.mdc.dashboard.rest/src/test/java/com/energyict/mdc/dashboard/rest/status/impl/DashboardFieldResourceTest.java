/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.engine.config.ComPortPool;

import com.jayway.jsonpath.JsonModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.dashboard.rest.status.ComServerStatusResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (17:27)
 */
public class DashboardFieldResourceTest extends DashboardApplicationJerseyTest {

    @Test
    public void testJava8Construct() throws Exception {
        ComPortPool comPortPool1 = mockComPortPool(1, "one");
        ComPortPool comPortPool2 = mockComPortPool(2, "two");
        ComPortPool comPortPool3 = mockComPortPool(3, "three");
        when(engineConfigurationService.findAllComPortPools()).thenReturn(Arrays.asList(comPortPool1, comPortPool2, comPortPool3));

        target("/field/comportpools").request().get(Map.class);
    }

    @Test
    public void testGetComSessionSuccessIndicators() {
        String response = target("/field/comsessionsuccessindicators").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<List<String>>get("$.successIndicators[*].localizedValue")).containsExactly("Broken", "Setup error", "Successful");
    }

    private ComPortPool mockComPortPool(long id, String name) {
        ComPortPool mock = mock(ComPortPool.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        return mock;
    }


}