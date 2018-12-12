/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.User;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Clock;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseCustomTaskRestTest extends FelixRestApplicationJerseyTest {
    @Mock
    protected CustomTaskService customTaskService;
    @Mock
    protected TaskService taskService;
    @Mock
    protected RestQueryService restQueryService;
    @Mock
    protected Clock clock;
    @Mock
    protected Thesaurus thesaurus;
    @Mock
    protected TimeService timeService;
    @Mock
    protected PropertyValueInfoService propertyValueInfoService;
    @Mock
    static SecurityContext securityContext;

    @Provider
    @Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }

   /* private DataValidationTaskBuilder initBuilderStub() {
        final Object proxyInstance = Proxy.newProxyInstance(DataValidationTaskBuilder.class.getClassLoader(), new Class<?>[]{DataValidationTaskBuilder.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (DataValidationTaskBuilder.class.isAssignableFrom(method.getReturnType())) {
                    return builderGetter.get();
                }

                return taskGetter.get();
            }

            private Supplier<DataValidationTask> taskGetter = () -> dataValidationTask;
            private Supplier<DataValidationTaskBuilder> builderGetter = () -> builder;
        });
        return (DataValidationTaskBuilder) proxyInstance;
    }
*/

    @Override
    public void setupMocks() {
        super.setupMocks();
        //when(validationService.newTaskBuilder()).thenReturn(builder);
        //when(meteringGroupsService.findEndDeviceGroup(1)).thenReturn(Optional.of(endDeviceGroup));
        //when(metrologyConfigurationService.findMetrologyContract(1)).thenReturn(Optional.of(metrologyContract));
        //when(metrologyConfigurationService.findMetrologyPurpose(Matchers.any(Long.class))).thenReturn(Optional.of(metrologyPurpose));
        //when(transactionService.execute(Matchers.any())).thenAnswer(invocation -> ((Transaction<?>) invocation.getArguments()[0]).perform());
    }

    @Override
    protected Application getApplication() {
        CustomTaskApplication app = new CustomTaskApplication() {
            //to mock security context
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> hashSet = new HashSet<>(super.getClasses());
                hashSet.add(SecurityRequestFilter.class);
                return Collections.unmodifiableSet(hashSet);
            }
        };
        app.setCustomTaskService(customTaskService);
        app.setTransactionService(transactionService);
        app.setTaskService(taskService);
        app.setRestQueryService(restQueryService);
        app.setClock(clock);
        app.setNlsService(nlsService);
        //app.setThesaurus(thesaurus);
        app.setTimeService(timeService);
        app.setPropertyValueInfoService(propertyValueInfoService);
        return app;
    }

    protected User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        return user;
    }

}
