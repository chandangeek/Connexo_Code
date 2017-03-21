/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractSecuritySupportTest {

    protected PropertySpecService propertySpecService;

    @Before
    public void setup() {
        propertySpecService = mock(PropertySpecService.class);
        //TODO somehow get the valuefactory instance (maybe mock it) into the propertyspec
        PropertySpecBuilderWizard.NlsOptions propertySpecBuilder = new PropertySpecBuilderImpl();
        when(propertySpecService.encryptedStringSpec()).thenReturn(propertySpecBuilder);
    }
}