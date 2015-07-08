package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.properties.*;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.protocol.api.*;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 7/07/2015
 * Time: 10:05
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectConfigurationPropertiesIsCompleteTest {

    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final String MY_PROPERTY1 = "myProperty1";
    private static final String MY_PROPERTY2 = "myProperty2";
    private static final String MY_PROPERTY3 = "myProperty3";

    public static final String PROTOCOL_DIALECT = "protocolDialect";

    private static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private LicenseService licenseService;
    @Mock
    private LicensedProtocolService licensedProtocolService;
    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private License license;
    @Mock
    DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol protocol;
    private DeviceProtocolDialect protocolDialect;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabaseWithMockedProtocolPluggableService("ProtocolDialectConfigurationPropertiesIsCompleteTest.mdc.device.config", false);
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Before
    @SuppressWarnings("unchecked")
    public void initializeMocks() {
        ValueFactory stringValueFactory = mock(ValueFactory.class);
        when(stringValueFactory.fromStringValue("someValue")).thenReturn("someValue");
        when(stringValueFactory.toStringValue("someValue")).thenReturn("someValue");
        when(stringValueFactory.fromStringValue("")).thenReturn("");
        when(stringValueFactory.toStringValue("")).thenReturn("");

        PropertySpec spec1 = mock(PropertySpec.class);
        when(spec1.getName()).thenReturn(MY_PROPERTY1);
        when(spec1.getValueFactory()).thenReturn(stringValueFactory);
        when(spec1.isRequired()).thenReturn(true);

        PropertySpec spec2 = mock(PropertySpec.class);
        when(spec2.getName()).thenReturn(MY_PROPERTY2);
        when(spec2.getValueFactory()).thenReturn(stringValueFactory);
        when(spec2.isRequired()).thenReturn(false);

        PropertySpec spec3 = mock(PropertySpec.class);
        when(spec3.getName()).thenReturn(MY_PROPERTY3);
        when(spec3.getValueFactory()).thenReturn(stringValueFactory);
        when(spec3.isRequired()).thenReturn(true);

        protocolDialect = mock(DeviceProtocolDialect.class);
        when(protocolDialect.getDeviceProtocolDialectName()).thenReturn(PROTOCOL_DIALECT);
        when(protocolDialect.getDisplayName()).thenReturn(PROTOCOL_DIALECT);
        when(protocolDialect.getPropertySpecs()).thenReturn(Arrays.asList(spec1,spec2, spec3));
        when(protocolDialect.getPropertySpec(MY_PROPERTY1)).thenReturn(spec1);
        when(protocolDialect.getPropertySpec(MY_PROPERTY2)).thenReturn(spec2);
        when(protocolDialect.getPropertySpec(MY_PROPERTY3)).thenReturn(spec3);

        when(protocol.getDeviceProtocolDialects()).thenReturn(Collections.singletonList(protocolDialect));

        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(protocol);
        when(inMemoryPersistence.getProtocolPluggableService().findDeviceProtocolPluggableClass(anyLong())).thenReturn(Optional.of(this.deviceProtocolPluggableClass));
    }

    @Test
    public void testAllRequiredHaveValues(){
        assertThat(protocolDialectConfigurationPropertiesWithAllRequiredProperties().isComplete()).isTrue();
    }

    @Test
    public void testNotAllRequiredHaveValues(){
        assertThat(protocolDialectConfigurationPropertiesWithSomeRequiredPropertiesHavingNoValue().isComplete()).isFalse();
    }

    private ProtocolDialectConfigurationProperties protocolDialectConfigurationPropertiesWithAllRequiredProperties(){
        ProtocolDialectConfigurationProperties properties = ProtocolDialectConfigurationPropertiesImpl.from(inMemoryPersistence.getDataModel(), deviceConfiguration, protocolDialect);
        properties.setProperty(MY_PROPERTY1, "someValue");
        properties.setProperty(MY_PROPERTY2, "someValue");
        properties.setProperty(MY_PROPERTY3, "someValue");
        return properties;
    }

    private ProtocolDialectConfigurationProperties protocolDialectConfigurationPropertiesWithSomeRequiredPropertiesHavingNoValue(){
        ProtocolDialectConfigurationProperties properties = ProtocolDialectConfigurationPropertiesImpl.from(inMemoryPersistence.getDataModel(), deviceConfiguration, protocolDialect);
        properties.setProperty(MY_PROPERTY1, "someValue");
        return  properties;
    }
}
