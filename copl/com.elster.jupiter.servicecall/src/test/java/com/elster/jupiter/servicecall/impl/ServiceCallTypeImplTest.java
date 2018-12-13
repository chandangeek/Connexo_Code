/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCallTypeImplTest {

    private static final String HANDLER_NAME = "handlerName";
    private Thesaurus thesaurus;

    @Mock
    private DataModel dataModel;
    @Mock
    private IServiceCallService serviceCallService;
    @Mock
    private ServiceCallHandler serviceRequestHandler;
    @Mock
    private ServiceCallImpl serviceCall;

    @Before
    public void setUp() {
        thesaurus = NlsModule.FakeThesaurus.INSTANCE;

        when(serviceCallService.findHandler(HANDLER_NAME)).thenReturn(Optional.of(serviceRequestHandler));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetServiceCallHandlerIsSafeWithAllowStateChange() {

        RuntimeException runtimeException = mock(RuntimeException.class);

        doThrow(runtimeException).when(serviceRequestHandler).allowStateChange(any(), any(), any());

        ServiceCallTypeImpl serviceCallType = new ServiceCallTypeImpl(dataModel, serviceCallService, thesaurus);
        serviceCallType.setHandlerName(HANDLER_NAME);

        ServiceCallHandler actualHandler = serviceCallType.getServiceCallHandler();

        assertThat(actualHandler.allowStateChange(serviceCall, DefaultState.CREATED, DefaultState.PENDING)).isFalse();

        verify(serviceCall).log(anyString(), eq(runtimeException));
    }

    @Test
    public void testGetServiceCallHandlerIsSafeWithOnStateChange() {

        RuntimeException runtimeException = mock(RuntimeException.class);

        doThrow(runtimeException).when(serviceRequestHandler).onStateChange(any(), any(), any());

        ServiceCallTypeImpl serviceCallType = new ServiceCallTypeImpl(dataModel, serviceCallService, thesaurus);
        serviceCallType.setHandlerName(HANDLER_NAME);

        ServiceCallHandler actualHandler = serviceCallType.getServiceCallHandler();

        try {
            actualHandler.onStateChange(serviceCall, DefaultState.CREATED, DefaultState.PENDING);
            fail("Exception expected");
        } catch (RuntimeException e) {
            // empty
        }

        verify(serviceCall).log(anyString(), eq(runtimeException));
    }


}