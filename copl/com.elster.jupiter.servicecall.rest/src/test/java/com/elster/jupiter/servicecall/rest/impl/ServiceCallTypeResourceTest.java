package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 2/11/16.
 */
public class ServiceCallTypeResourceTest extends ServiceCallApplicationTest {


    @Test
    public void testGetAllServiceCallTypes() throws Exception {
        ServiceCallType serviceCallType = mock(ServiceCallType.class);
        when(serviceCallType.getName()).thenReturn("Mbus 1");
        Finder<ServiceCallType> serviceCallTypeFinder = mockFinder(Collections.singletonList(serviceCallType));
        when(serviceCallService.getServiceCallTypes()).thenReturn(serviceCallTypeFinder);

        Response response = target("/servicecalltypes").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(1);
    }
}
