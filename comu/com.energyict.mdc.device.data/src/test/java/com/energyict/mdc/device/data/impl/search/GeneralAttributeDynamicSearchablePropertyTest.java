/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.search.SearchableProperty;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeneralAttributeDynamicSearchablePropertyTest extends AbstractDynamicSearchablePropertyTest{

    private DeviceProtocolPluggableClass pluggableClass;

    @Before
    public void initializeMocks() {
        pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass.getJavaClassName()).thenReturn("someProtocol");
        when(pluggableClass.getName()).thenReturn("Test protocol");
    }

    protected SearchableProperty getTestInstance() {
        return new GeneralAttributeDynamicSearchableProperty(this.getThesaurus())
                .init(this.domain, this.group, this.propertySpec, this.searchableProperty, this.pluggableClass);
    }

}