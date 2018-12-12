/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompletionOptionsImplTest {

    private static final long SERVICE_CALL_ID = 10L;
    private static final String DESTINATION_SPEC_NAME = "DESTINATION_SPEC_NAME";

    @Mock
    private ServiceCall serviceCall;
    @Mock
    private CompletionOptionsServiceCallDomainExtension domainExtension;
    @Mock
    DestinationSpec destinationSpec;

    private CompletionOptions completionOptions;

    @Before
    public void setUp() throws Exception {
        when(serviceCall.getId()).thenReturn(SERVICE_CALL_ID);
        when(serviceCall.getExtensionFor(any(CompletionOptionsCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));

        when(destinationSpec.getName()).thenReturn(DESTINATION_SPEC_NAME);
        completionOptions = new CompletionOptionsImpl(serviceCall);
    }

    @Test
    public void whenFinishedSendCompletionMessageTest() {
        // Business method
        completionOptions.whenFinishedSendCompletionMessageWith(Long.toString(SERVICE_CALL_ID), destinationSpec);

        // Asserts
        verify(serviceCall).update(domainExtension);
        verify(domainExtension).setDestinationIdentification(Long.toString(SERVICE_CALL_ID));
        verify(domainExtension).setDestinationSpec(destinationSpec.getName());
    }
}
