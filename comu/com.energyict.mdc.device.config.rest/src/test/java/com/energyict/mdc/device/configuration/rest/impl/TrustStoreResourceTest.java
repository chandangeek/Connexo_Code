/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.TrustStore;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 4/3/17.
 */
public class TrustStoreResourceTest extends DeviceConfigurationApplicationJerseyTest {

    @Test
    public void testGetTrustStores() throws Exception {
        TrustStore trustStore1 = mockTrustStore(1L, "one");
        TrustStore trustStore2 = mockTrustStore(2L, "two");
        TrustStore trustStore3 = mockTrustStore(3L, "three");

        when(securityManagementService.getAllTrustStores()).thenReturn(Arrays.asList(trustStore1, trustStore2, trustStore3));

        Response response = target("/truststores").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List>get("$.trustStores")).hasSize(3);
        assertThat(jsonModel.<List<String>>get("$.trustStores[*].name")).containsOnly("one", "two", "three");
    }

    private TrustStore mockTrustStore(long id, String name) {
        TrustStore trustStore = mock(TrustStore.class);
        when(trustStore.getName()).thenReturn(name);
        when(trustStore.getId()).thenReturn(id);
        return trustStore;
    }
}
