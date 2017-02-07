/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.search.SearchableProperty;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionDynamicSearchablePropertyTest extends AbstractDynamicSearchablePropertyTest {


    private ConnectionTypePluggableClass pluggableClass;

    @Before
    public void initializeMocks() {
        pluggableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggableClass.getJavaClassName()).thenReturn("someProtocol");
        when(pluggableClass.getName()).thenReturn("Test protocol");
    }

    protected SearchableProperty getTestInstance() {
        return new ConnectionDynamicSearchableProperty(this.getThesaurus())
                .init(this.domain, this.group, this.propertySpec, this.searchableProperty, this.pluggableClass);
    }

}