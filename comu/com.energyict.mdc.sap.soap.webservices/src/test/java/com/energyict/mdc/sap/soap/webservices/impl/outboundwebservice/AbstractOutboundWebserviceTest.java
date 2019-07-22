/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import java.lang.reflect.Field;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractOutboundWebserviceTest {

    private static final String URL = "url";

    @Mock
    protected WebServiceActivator webServiceActivator;
    @Mock
    protected WebServicesService webServicesService;
    @Mock
    protected WebServiceCallOccurrence webServiceCallOccurrence;
    @Mock
    protected OutboundEndPointProvider.RequestSender requestSender;
    @Mock
    protected EndPointConfigurationService endPointConfigurationService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    public String getURL() {
        return URL;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected static void inject(Class<?> clazz, Object instance, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}