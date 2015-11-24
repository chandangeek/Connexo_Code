package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.BasicPropertySpec;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecurityProperties;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport;

import java.sql.SQLException;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link SmartMeterProtocolSecuritySupportAdapter} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/01/13
 * Time: 12:00
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartMeterProtocolSecuritySupportAdapterTest {

    @Mock
    private PropertiesAdapter propertiesAdapter;

    private InMemoryPersistence inMemoryPersistence;
    private ProtocolPluggableServiceImpl protocolPluggableService;
    private DataModel dataModel;

    @Before
    public void initializeDatabaseAndMocks() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase(
                "SmartMeterProtocolSecuritySupportAdapterTest.mdc.protocol.pluggable",
                dataModel1 -> {
                    dataModel1.persist(new SecuritySupportAdapterMappingImpl(SimpleTestSmartMeterProtocol.class.getName(), SimpleTestDeviceSecuritySupport.class.getName()));
                    dataModel1.persist(new SecuritySupportAdapterMappingImpl(SecondSimpleTestSmartMeterProtocol.class.getName(), "com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.NotAKnownDeviceSecuritySupportClass"));
                    dataModel1.persist(new SecuritySupportAdapterMappingImpl(ThirdSimpleTestSmartMeterProtocol.class.getName(), ThirdSimpleTestSmartMeterProtocol.class.getName()));
                });
        this.protocolPluggableService = this.inMemoryPersistence.getProtocolPluggableService();
        this.dataModel = this.protocolPluggableService.getDataModel();
        this.initializeMocks();
    }

    private void initializeMocks() {
        PropertySpecService propertySpecService = inMemoryPersistence.getPropertySpecService();
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName(), false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName(), false, new StringFactory()));
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.SECOND.javaName(), false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.SECOND.javaName(), false, new StringFactory()));
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.THIRD.javaName(), false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.THIRD.javaName(), false, new StringFactory()));
        DeviceProtocolSecurityService deviceProtocolSecurityService = this.inMemoryPersistence.getDeviceProtocolSecurityService();
        SimpleTestDeviceSecuritySupport securitySupport = new SimpleTestDeviceSecuritySupport(propertySpecService);
        when(deviceProtocolSecurityService.createDeviceProtocolSecurityFor(SimpleTestDeviceSecuritySupport.class.getCanonicalName())).thenReturn(securitySupport);
        doThrow(DeviceProtocolAdapterCodingExceptions.class).when(deviceProtocolSecurityService).createDeviceProtocolSecurityFor("com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.NotAKnownDeviceSecuritySupportClass");
        when(deviceProtocolSecurityService.createDeviceProtocolSecurityFor(ThirdSimpleTestSmartMeterProtocol.class.getCanonicalName())).thenReturn(new ThirdSimpleTestSmartMeterProtocol());
    }

    @After
    public void cleanUpDataBase () throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void testKnownMeterProtocol() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        new SmartMeterProtocolSecuritySupportAdapter(
                simpleTestMeterProtocol,
                this.inMemoryPersistence.getPropertySpecService(),
                this.protocolPluggableService,
                this.propertiesAdapter,
                new SecuritySupportAdapterMappingFactoryImpl(this.dataModel));

        // all is safe if no errors occur
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownMeterProtocol() {
        SmartMeterProtocol meterProtocol = mock(SmartMeterProtocol.class);
        try {
            new SmartMeterProtocolSecuritySupportAdapter(
                    meterProtocol,
                    this.inMemoryPersistence.getPropertySpecService(),
                    this.protocolPluggableService,
                    this.propertiesAdapter,
                    new SecuritySupportAdapterMappingFactoryImpl(this.dataModel));
        } catch (DeviceProtocolAdapterCodingExceptions e) {
            if (!e.getMessageSeed().equals(MessageSeeds.NON_EXISTING_MAP_ELEMENT)) {
                fail("Exception should have indicated that the given meterProtocol is not known in the adapter, but was " + e.getMessage());
            }
            throw e;
        }
        // should have gotten the exception
    }

    @Test
    public void testNotADeviceSecuritySupportClass() {
        SmartMeterProtocol meterProtocol = new ThirdSimpleTestSmartMeterProtocol();
        final SmartMeterProtocolSecuritySupportAdapter spy = spy(new SmartMeterProtocolSecuritySupportAdapter(
                meterProtocol,
                this.inMemoryPersistence.getPropertySpecService(),
                this.protocolPluggableService,
                this.propertiesAdapter,
                new SecuritySupportAdapterMappingFactoryImpl(this.dataModel)));

        // asserts
        verify(spy, never()).setLegacySecuritySupport(any(DeviceProtocolSecurityCapabilities.class));
        verify(spy, never()).setLegacySecurityPropertyConverter(any(LegacySecurityPropertyConverter.class));
    }

    @Test
    public void setSecurityPropertySetTest() {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);

        SimpleTestSmartMeterProtocol simpleTestSmartMeterProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter = new SmartMeterProtocolSecuritySupportAdapter(
                simpleTestSmartMeterProtocol,
                this.inMemoryPersistence.getPropertySpecService(),
                this.protocolPluggableService,
                this.propertiesAdapter,
                new SecuritySupportAdapterMappingFactoryImpl(this.dataModel));

        // business method
        meterProtocolSecuritySupportAdapter.setSecurityPropertySet(deviceProtocolSecurityPropertySet);

        // asserts
        verify(propertiesAdapter, times(1)).copyProperties(Matchers.<TypedProperties>any());
    }

}