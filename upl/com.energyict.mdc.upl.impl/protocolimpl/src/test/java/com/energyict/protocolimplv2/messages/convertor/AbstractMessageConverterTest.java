/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.propertyspec.MockPropertySpecService;
import org.mockito.Mock;

public class AbstractMessageConverterTest {

    protected PropertySpecService propertySpecService = new MockPropertySpecService();
    @Mock
    protected NlsService nlsService;
    @Mock
    protected Converter converter;

}