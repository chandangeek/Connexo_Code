package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.search.SearchableProperty;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeneralAttributeDynamicSearchablePropertyTest extends AbstractDynamicSearchablePropertyTest{

    private DeviceProtocolPluggableClass pluggableClass;

    @Before
    public void initializeMocks() {
        pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass.getJavaClassName()).thenReturn("someProtocol");
        when(pluggableClass.getName()).thenReturn("Test protocol");
    }

    protected SearchableProperty getTestInstance() {
        return new GeneralAttributeDynamicSearchableProperty()
                .init(this.domain, this.group, this.propertySpec, this.pluggableClass);
    }
}
