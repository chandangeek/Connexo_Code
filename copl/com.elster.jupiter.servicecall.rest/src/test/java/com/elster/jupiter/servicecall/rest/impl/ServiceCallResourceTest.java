package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFinder;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallType;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceCallResourceTest extends ServiceCallApplicationTest {

    @Test
    public void getAllServiceCalls() throws Exception {
        mockSetup();
        Response response = target("/servicecalls").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("serviceCalls[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("serviceCalls[0].type")).isEqualTo("Mbus 1");
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
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    private void mockSetup() {
        ServiceCall serviceCall = mockServiceCall(1L);
        ServiceCall child = mockServiceCall(2L);
        when(child.getParent()).thenReturn(Optional.of(serviceCall));
        ServiceCallFinder finder = mock(ServiceCallFinder.class);
        ServiceCallFinder childrenFinder = mock(ServiceCallFinder.class);
        ServiceCallFinder childrenOfChildFinder = mock(ServiceCallFinder.class);
        when(finder.find()).thenReturn(Collections.singletonList(serviceCall));
        when(serviceCallService.getServiceCall(1)).thenReturn(Optional.of(serviceCall));
        when(serviceCallService.getServiceCallFinder()).thenReturn(finder);
        when(serviceCall.findChildren()).thenReturn(childrenFinder);
        when(serviceCall.findChildren().find()).thenReturn(Collections.singletonList(child));
        when(child.findChildren()).thenReturn(childrenOfChildFinder);
        when(child.findChildren().find()).thenReturn(Collections.emptyList());
        when(serviceCallService.getServiceCall(2)).thenReturn(Optional.of(child));
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
        when(serviceCall.getCreationTime()).thenReturn(Instant.EPOCH);
        when(serviceCall.getLastModificationTime()).thenReturn(Instant.EPOCH);
        when(serviceCall.getExternalReference()).thenReturn(Optional.empty());
        when(serviceCall.getLastCompletedTime()).thenReturn(Optional.empty());
        when(serviceCall.getOrigin()).thenReturn(Optional.empty());
        when(serviceCall.getParent()).thenReturn(Optional.empty());
        when(serviceCall.getState()).thenReturn(DefaultState.CREATED);
        return serviceCall;
    }
}
