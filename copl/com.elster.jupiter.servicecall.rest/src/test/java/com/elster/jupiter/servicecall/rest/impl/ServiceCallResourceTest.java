/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceCallResourceTest extends ServiceCallApplicationTest {

    @Test
    public void getAllServiceCalls() throws Exception {
        mockSetup();
        Response response = target("/servicecalls").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("serviceCalls[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("serviceCalls[0].type")).isEqualTo("Mbus 1 (v1)");
    }

    @Test
    public void getChildren() throws Exception {
        mockSetup();
        Response response = target("/servicecalls/1/children").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("serviceCalls[0].id")).isEqualTo(2);
    }

    @Test
    public void getServiceCall() throws Exception {
        mockSetup();
        Response response = target("/servicecalls/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("id")).isEqualTo(1);
    }

    @Test
    public void getServiceCallWithParent() throws Exception {
        mockSetup();
        Response response = target("/servicecalls/2").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("id")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("parents[0].id")).isEqualTo(1);
    }

    @Test
    public void getNonExistingServiceCall() throws  Exception {
        when(serviceCallService.getServiceCall(3)).thenReturn(Optional.empty());
        Response response = target("/servicecalls/3").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    private void mockSetup() {
        ServiceCall serviceCall = mockServiceCall(1L);
        when(serviceCall.getTargetObject()).thenReturn(Optional.empty());
        ServiceCall child = mockServiceCall(2L);
        when(child.getTargetObject()).thenReturn(Optional.empty());
        when(child.getParent()).thenReturn(Optional.of(serviceCall));
        List<ServiceCall> list = new ArrayList<>();
        list.add(serviceCall);
        list.add(child);

        Finder<ServiceCall> finder = mockFinder(Collections.unmodifiableList(list));
        Finder<ServiceCall> childrenFinder = mockFinder(Collections.singletonList(child));
        Finder<ServiceCall> childrenOfChildFinder = mockFinder(Collections.emptyList());
        when(serviceCallService.getServiceCall(1)).thenReturn(Optional.of(serviceCall));
        when(serviceCallService.getServiceCallFinder(any(ServiceCallFilter.class))).thenReturn(finder);
        when(serviceCall.findChildren(any(ServiceCallFilter.class))).thenReturn(childrenFinder);
        when(serviceCall.getTargetObject()).thenReturn(Optional.empty());
        when(child.findChildren(any(ServiceCallFilter.class))).thenReturn(childrenOfChildFinder);
        when(child.getTargetObject()).thenReturn(Optional.empty());
        when(serviceCallService.getServiceCall(2)).thenReturn(Optional.of(child));
        when(referenceResolver.resolve(any())).thenReturn(Optional.empty());
    }

    private ServiceCall mockServiceCall(long id) {
        ServiceCall serviceCall = mock(ServiceCall.class);
        ServiceCallType serviceCallType = mock(ServiceCallType.class);
        when(serviceCall.getId()).thenReturn(id);
        when(serviceCall.getType()).thenReturn(serviceCallType);
        ServiceCallLifeCycle serviceCallLifeCycle = mock(ServiceCallLifeCycle.class);
        when(serviceCallLifeCycle.getId()).thenReturn(1L);
        when(serviceCallLifeCycle.getName()).thenReturn("default");
        when(serviceCallType.getName()).thenReturn("Mbus 1");
        when(serviceCallType.getVersionName()).thenReturn("v1");
        when(serviceCall.getCreationTime()).thenReturn(Instant.EPOCH);
        when(serviceCall.getLastModificationTime()).thenReturn(Instant.EPOCH);
        when(serviceCall.getExternalReference()).thenReturn(Optional.empty());
        when(serviceCall.getLastCompletedTime()).thenReturn(Optional.empty());
        when(serviceCall.getOrigin()).thenReturn(Optional.empty());
        when(serviceCall.getParent()).thenReturn(Optional.empty());
        when(serviceCall.getState()).thenReturn(DefaultState.CREATED);
        return serviceCall;
    }

    <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }
}
