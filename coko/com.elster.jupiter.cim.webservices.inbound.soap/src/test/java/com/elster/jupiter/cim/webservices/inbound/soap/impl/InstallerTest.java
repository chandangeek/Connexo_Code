/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;

import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstallerTest {

    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceCallService serviceCallService;
    @Mock
    private RegisteredCustomPropertySet registeredCustomPropertySet;
    @Mock
    private DataModelUpgrader dataModelUpgrader;
    @Mock
    private Logger logger;
    @Mock
    private MessageService messageService;

    @Test
    public void testInstall() {
        when(serviceCallService.findServiceCallType(anyString(), anyString())).thenReturn(Optional.empty());
        Installer installer = new Installer(serviceCallService, customPropertySetService, messageService);
        when(customPropertySetService.findActiveCustomPropertySet(anyString()))
                .thenReturn(Optional.of(registeredCustomPropertySet));
        when(messageService.getDestinationSpec(anyString())).thenReturn(Optional.of(mock(DestinationSpec.class)));

        installer.install(dataModelUpgrader, logger);

        verify(serviceCallService, times(ServiceCallTypes.values().length)).createServiceCallType(anyString(),
                anyString());
    }
}
