/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCustomPropertySetTest {

    protected static final String EXPECTED_TRANSLATED_VALUE = "expected value";

    @Mock
    protected NlsService nlsService;

    @Mock
    protected PropertySpecService propertySpecService;

    @Mock
    protected Thesaurus thesaurus;

    @Mock
    protected NlsMessageFormat nlsMessageFormat;

    protected void setup() {
        propertySpecService =  mock(PropertySpecService.class,
                withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS));
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        when(nlsMessageFormat.format()).thenReturn(EXPECTED_TRANSLATED_VALUE);
    }
}