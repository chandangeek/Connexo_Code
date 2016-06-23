package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.DeviceService;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Mock;

public class UsagePointApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    MeteringService meteringService;
    @Mock
    DeviceService deviceService;
    @Mock
    TransactionService transactionService;
    @Mock
    static SecurityContext securityContext;

    @Override
    protected Application getApplication() {
        UsagePointApplication app = new UsagePointApplication() {
            //to mock security context
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> hashSet = new HashSet<>(super.getClasses());
                hashSet.add(SecurityRequestFilter.class);
                return Collections.unmodifiableSet(hashSet);
            }
        };
        app.setMeteringService(meteringService);
        app.setTransactionService(transactionService);
        app.setNlsService(nlsService);
        app.setDeviceService(deviceService);
        return app;
    }

    @Provider
    @Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }
}
