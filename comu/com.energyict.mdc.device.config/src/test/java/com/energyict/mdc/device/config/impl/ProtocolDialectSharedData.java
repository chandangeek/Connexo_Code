package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.google.common.base.Strings;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProtocolDialectSharedData {
    private static final String MY_PROPERTY = "myProperty";
    public static final String PROTOCOL_DIALECT = "protocolDialect";
    public static final String VERY_LARGE_STRING = Strings.repeat("0123456789", 10000); // String containing 100_000 characters which >> 4K

    private static PropertySpec propertySpec;
    private static DeviceProtocolDialect protocolDialect;
    private static ValueFactory valueFactory;

    private interface State {
        PropertySpec getPropertySpec();

        DeviceProtocolDialect getProtocolDialect();

        ValueFactory getValueFactory();
    }

    private static State actual;

    public ProtocolDialectSharedData() {
        if (actual == null) {
            actual = new State() {
                @Override
                public PropertySpec getPropertySpec() {
                    return propertySpec;
                }

                @Override
                public DeviceProtocolDialect getProtocolDialect() {
                    return protocolDialect;
                }

                @Override
                public ValueFactory getValueFactory() {
                    return valueFactory;
                }
            };
            propertySpec = mock(PropertySpec.class);
            protocolDialect = mock(DeviceProtocolDialect.class);
            when(protocolDialect.getDeviceProtocolDialectDisplayName()).thenReturn(PROTOCOL_DIALECT);
            valueFactory = mock(ValueFactory.class);
            when(getProtocolDialect().getPropertySpec(MY_PROPERTY)).thenReturn(Optional.of(getPropertySpec()));
            when(getPropertySpec().getValueFactory()).thenReturn(getValueFactory());
            when(getProtocolDialect().getDeviceProtocolDialectName()).thenReturn(PROTOCOL_DIALECT);
            when(getValueFactory().fromStringValue("15")).thenReturn(15);
            when(getValueFactory().toStringValue(15)).thenReturn("15");
            when(getValueFactory().fromStringValue(VERY_LARGE_STRING)).thenReturn(VERY_LARGE_STRING);
            when(getValueFactory().toStringValue(VERY_LARGE_STRING)).thenReturn(VERY_LARGE_STRING);
        }
    }

    PropertySpec getPropertySpec() {
        return actual.getPropertySpec();
    }

    DeviceProtocolDialect getProtocolDialect() {
        return actual.getProtocolDialect();
    }

    ValueFactory getValueFactory() {
        return actual.getValueFactory();
    }

    void invalidate() {
        actual = null;
    }

}
