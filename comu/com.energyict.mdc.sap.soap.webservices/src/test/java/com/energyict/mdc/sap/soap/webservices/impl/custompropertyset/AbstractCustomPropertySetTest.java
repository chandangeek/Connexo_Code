/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCustomPropertySetTest {

    protected static final String EXPECTED_TRANSLATED_VALUE = "expected value";

    @Mock
    protected NlsService nlsService;

    @Mock
    protected Thesaurus thesaurus;

    @Mock
    protected NlsMessageFormat nlsMessageFormat;

    protected void setup() {
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        when(nlsMessageFormat.format()).thenReturn(EXPECTED_TRANSLATED_VALUE);
    }
}