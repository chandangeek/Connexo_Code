/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.PropertySpecService;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCustomPropertySetTest {

    protected static final String EXPECTED_TRANSLATED_VALUE = "expected value";

    @Mock
    protected NlsService nlsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected PropertySpecService propertySpecService;

    @Mock
    protected Thesaurus thesaurus;

    @Mock
    protected NlsMessageFormat nlsMessageFormat;

    protected void setup() {
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        when(nlsMessageFormat.format()).thenReturn(EXPECTED_TRANSLATED_VALUE);
    }
}