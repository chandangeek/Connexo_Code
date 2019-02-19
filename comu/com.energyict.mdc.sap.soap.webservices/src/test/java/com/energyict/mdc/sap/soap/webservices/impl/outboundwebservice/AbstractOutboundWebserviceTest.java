/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    public String getURL() {
        return URL;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }
}