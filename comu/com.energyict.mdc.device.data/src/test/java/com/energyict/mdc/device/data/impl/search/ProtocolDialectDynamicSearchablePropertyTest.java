package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.search.SearchableProperty;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectDynamicSearchablePropertyTest extends AbstractDynamicSearchablePropertyTest{

    private ProtocolDialectSearchableProperty.ProtocolDialect protocolDialect;

    @Before
    public void initializeMocks() {
        this.protocolDialect = new ProtocolDialectSearchableProperty.ProtocolDialect(mock(DeviceProtocolPluggableClass.class), mock(DeviceProtocolDialect.class));
    }

    protected SearchableProperty getTestInstance() {
        return new ProtocolDialectDynamicSearchableProperty()
                .init(this.domain, this.group, this.propertySpec, this.protocolDialect, "some_table");
    }
}
