/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.search.SearchableProperty;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ProtocolDialectDynamicSearchablePropertyTest extends AbstractDynamicSearchablePropertyTest {

    private ProtocolDialectSearchableProperty.ProtocolDialect protocolDialect;

    @Before
    public void initializeMocks() {
        this.protocolDialect = new ProtocolDialectSearchableProperty.ProtocolDialect(mock(DeviceProtocolPluggableClass.class), mock(DeviceProtocolDialect.class));
    }

    protected SearchableProperty getTestInstance() {
        return new ProtocolDialectDynamicSearchableProperty(this.getThesaurus())
                .init(this.domain, this.group, this.propertySpec, this.searchableProperty, this.protocolDialect, "some_table");
    }

    @Override
    public void testTranslation() {
        super.testTranslation();

        // Additional asserts
        verify(this.protocolDialect.getProtocolDialect()).getDisplayName();
    }

}