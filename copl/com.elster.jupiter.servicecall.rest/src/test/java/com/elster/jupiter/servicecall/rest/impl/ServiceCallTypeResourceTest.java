/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.Status;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 2/11/16.
 */
public class ServiceCallTypeResourceTest extends ServiceCallApplicationTest {


    @Test
    public void testGetAllServiceCallTypes() throws Exception {
        mockServiceCallType(1L);

        Response response = target("/servicecalltypes").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("serviceCallTypes[0].name")).isEqualTo("Mbus 1");
    }

    @Test
    public void testChangeLogLevel() throws Exception {
        mockServiceCallType(1L);

        ServiceCallTypeInfo info = new ServiceCallTypeInfo();
        info.id = 666L; // fake id
        info.version = 1L;
        info.logLevel = new IdWithDisplayValueInfo<>();
        info.logLevel.id = LogLevel.SEVERE.name();

        Response response = target("/servicecalltypes/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

    }

    private ServiceCallType mockServiceCallType(long id) {
        ServiceCallType serviceCallType = mock(ServiceCallType.class);
        ServiceCallLifeCycle serviceCallLifeCycle = mock(ServiceCallLifeCycle.class);
        when(serviceCallLifeCycle.getId()).thenReturn(1L);
        when(serviceCallLifeCycle.getName()).thenReturn("default");
        when(serviceCallType.getName()).thenReturn("Mbus 1");
        Finder<ServiceCallType> serviceCallTypeFinder = mockFinder(Collections.singletonList(serviceCallType));
        when(serviceCallService.getServiceCallTypes()).thenReturn(serviceCallTypeFinder);
        when(serviceCallService.findAndLockServiceCallType(id, 1L)).thenReturn(Optional.of(serviceCallType));
        when(serviceCallType.getServiceCallLifeCycle()).thenReturn(serviceCallLifeCycle);
        when(serviceCallType.getLogLevel()).thenReturn(LogLevel.WARNING);
        when(serviceCallType.getStatus()).thenReturn(Status.ACTIVE);
        when(serviceCallType.getId()).thenReturn(id);
        when(serviceCallType.getVersion()).thenReturn(1L);
        return serviceCallType;
    }
}
