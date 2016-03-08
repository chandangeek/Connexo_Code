package com.elster.jupiter.servicecall.rest.impl;

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
        mockServiceCall(1L);
        Response response = target("/servicecalls").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("serviceCalls[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("serviceCalls[0].type")).isEqualTo("Mbus 1");
    }

    private ServiceCall mockServiceCall(long id) {
        ServiceCall serviceCall = mock(ServiceCall.class);
        ServiceCallType serviceCallType = mock(ServiceCallType.class);
        ServiceCallLifeCycle serviceCallLifeCycle = mock(ServiceCallLifeCycle.class);
        when(serviceCallLifeCycle.getId()).thenReturn(1L);
        when(serviceCallLifeCycle.getName()).thenReturn("default");
        when(serviceCallType.getName()).thenReturn("Mbus 1");
        ServiceCallFinder finder = mock(ServiceCallFinder.class);
        when(finder.find()).thenReturn(Collections.singletonList(serviceCall));
        when(serviceCallService.getServiceCallFinder()).thenReturn(finder);
        when(serviceCall.getId()).thenReturn(id);
        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(serviceCall.getCreationTime()).thenReturn(Instant.EPOCH);
        when(serviceCall.getLastModificationTime()).thenReturn(Instant.EPOCH);
        when(serviceCall.getExternalReference()).thenReturn(Optional.empty());

        return serviceCall;
    }
}
