/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallLog;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 3/2/16.
 */
public class ServcieCallLogsResourceTest extends ServiceCallApplicationTest {

    @Test
    public void testGetAllLogs() throws Exception {
        ServiceCall serviceCall = mockServiceCall(3L);
        ServiceCallLog first = mockLog(serviceCall, LogLevel.SEVERE, "First");
        ServiceCallLog second = mockLog(serviceCall, LogLevel.INFO, "Second");
        ServiceCallLog third = mockLog(serviceCall, LogLevel.FINEST, "Third");
        Finder<ServiceCallLog> serviceCallLogFinder = mockFinder(Arrays.asList(first, second, third));
        when(serviceCall.getLogs()).thenReturn(serviceCallLogFinder);
        Response response = target("/servicecalls/3/logs").request().get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<String>get("$.logs[0].message")).isEqualTo("First");
        assertThat(jsonModel.<String>get("$.logs[0].logLevel")).isEqualTo("Severe");
        assertThat(jsonModel.<String>get("$.logs[1].message")).isEqualTo("Second");
        assertThat(jsonModel.<String>get("$.logs[1].logLevel")).isEqualTo("Information");
        assertThat(jsonModel.<String>get("$.logs[2].message")).isEqualTo("Third");
        assertThat(jsonModel.<String>get("$.logs[2].logLevel")).isEqualTo("Finest");
    }

    private ServiceCallLog mockLog(ServiceCall serviceCall, LogLevel level, String message) {
        ServiceCallLog mock = mock(ServiceCallLog.class);
        when(mock.getLogLevel()).thenReturn(level);
        when(mock.getMessage()).thenReturn(message);
        when(mock.getTime()).thenReturn(Instant.now());
        when(mock.getServiceCall()).thenReturn(serviceCall);
        return mock;
    }

    private ServiceCall mockServiceCall(long id) {
        ServiceCall mock = mock(ServiceCall.class);
        when(mock.getId()).thenReturn(id);
        when(serviceCallService.getServiceCall(id)).thenReturn(Optional.of(mock));
        return mock;
    }
}
