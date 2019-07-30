/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.Status;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 2/11/16.
 */
public class ServiceCallTypeResourceTest extends ServiceCallApplicationTest {

    private static final String SERVICE_CALL_TYPE_DESTINATION_NAME = "TESTSC";
    private static final String QUEUE_NAME = "testQueue";

    @Test
    public void testGetAllServiceCallTypes() throws Exception {
        mockServiceCallType(1L);

        Response response = target("/servicecalltypes").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("serviceCallTypes[0].name")).isEqualTo("Mbus 1");
        assertThat(jsonModel.<String>get("serviceCallTypes[0].destination")).isEqualTo(SERVICE_CALL_TYPE_DESTINATION_NAME);
        assertThat(jsonModel.<Integer>get("serviceCallTypes[0].priority")).isEqualTo(10);
    }

    @Test
    public void testUpdateServiceCallType() throws Exception {
        ServiceCallType serviceCallType = mockServiceCallType(1L);

        ServiceCallTypeInfo info = new ServiceCallTypeInfo();
        info.id = 666L; // fake id
        info.version = 1L;
        info.logLevel = new IdWithDisplayValueInfo<>();
        info.logLevel.id = LogLevel.SEVERE.name();
        info.destination = QUEUE_NAME;
        info.priority = 20;

        Response response = target("/servicecalltypes/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(serviceCallType).setDestination(QUEUE_NAME);
        verify(serviceCallType).setPriority(20);
        verify(serviceCallType).save();

    }

    @Test
    public void testGetCompatibleQueues() throws Exception {
        mockServiceCallType(1L);

        Response response = target("/servicecalltypes/compatiblequeues").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.getJsonObject().toString()).isEqualTo("[{\"isDefault\":true,\"name\":\"testQueue\"}]");
    }

    private ServiceCallType mockServiceCallType(long id) {
        List<DestinationSpec> specs = new ArrayList<>();
        DestinationSpec destination = mock(DestinationSpec.class);
        when(destination.getName()).thenReturn(QUEUE_NAME);
        when(destination.isDefault()).thenReturn(true);
        when(messageService.getDestinationSpec(anyString())).thenReturn(Optional.of(destination));
        specs.add(destination);
        ServiceCallType serviceCallType = mock(ServiceCallType.class);
        ServiceCallLifeCycle serviceCallLifeCycle = mock(ServiceCallLifeCycle.class);
        when(serviceCallLifeCycle.getId()).thenReturn(1L);
        when(serviceCallLifeCycle.getName()).thenReturn("default");
        when(serviceCallType.getName()).thenReturn("Mbus 1");
        when(serviceCallType.getDestinationName()).thenReturn(SERVICE_CALL_TYPE_DESTINATION_NAME);
        when(serviceCallType.getPriority()).thenReturn(10);
        Finder<ServiceCallType> serviceCallTypeFinder = mockFinder(Collections.singletonList(serviceCallType));
        when(serviceCallService.getServiceCallTypes()).thenReturn(serviceCallTypeFinder);
        when(serviceCallService.findAndLockServiceCallType(id, 1L)).thenReturn(Optional.of(serviceCallType));
        when(serviceCallService.getCompatibleQueues4()).thenReturn(specs);
        when(serviceCallType.getServiceCallLifeCycle()).thenReturn(serviceCallLifeCycle);
        when(serviceCallType.getLogLevel()).thenReturn(LogLevel.WARNING);
        when(serviceCallType.getStatus()).thenReturn(Status.ACTIVE);
        when(serviceCallType.getId()).thenReturn(id);
        when(serviceCallType.getVersion()).thenReturn(1L);
        when(serviceCallType.getApplication()).thenReturn(Optional.empty());
        return serviceCallType;
    }
}
