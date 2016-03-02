package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallLog;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

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
