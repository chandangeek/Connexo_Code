package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.impl.ServiceLocator;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceRegisterReadingNotSupported;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.LegacyPropertySpecSupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock.HhuEnabledMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock.RegisterSupportedMeterProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import org.fest.assertions.core.Condition;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;


/**
 * Tests the adapter between a standard {@link MeterProtocol} and the new {@link DeviceProtocol}.
 *
 * @author gna
 * @since 29/03/12 - 11:16
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterProtocolAdapterTest {

    private static final String PROPERTY_SPEC_NAME = "whatever";
    private static final String PROTOCOL_CLASS = "com.energyict.comserver.adapters.meterprotocol.SimpleTestMeterProtocol";
    private static final String AS220_PROTOCOL = "com.energyict.protocolimpl.dlms.as220.AS220";

    @Mock
    private CapabilityAdapterMappingFactory capabilityAdapterMappingFactory;
    @Mock
    private Environment environment;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private LegacySecurityPropertyConverter legacySecurityPropertyConverter;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    @Mock
    private SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    private EventService eventService;
    @Mock
    private PluggableService pluggableService;
    @Mock
    private RelationService relationService;
    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    @Mock
    private ConnectionTypeService connectionTypeService;

    private ProtocolPluggableService protocolPluggableService;

    @Before
    public void initializeMocks () {
        when(securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol(PROTOCOL_CLASS)).
        thenReturn("com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport");
        when(capabilityAdapterMappingFactory.getCapabilitiesMappingForDeviceProtocol(MockDeviceProtocol.class.getCanonicalName())).thenReturn(6);  //6 = master and session capability
        when(this.deviceProtocolSecurityService.createDeviceProtocolSecurityFor("com.energyict.comserver.adapters.common.SimpleTestDeviceSecuritySupport")).
                thenReturn(new SimpleTestDeviceSecuritySupport());
        protocolPluggableService = new ProtocolPluggableServiceImpl(this.ormService, this.eventService, this.pluggableService, this.relationService, this.deviceProtocolService, this.inboundDeviceProtocolService, this.connectionTypeService);
    }

    @Before
    public void setUpEnvironment () {
        Environment.DEFAULT.set(this.environment);
        when(this.environment.getErrorMsg(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });
    }

    @After
    public void tearDownEnvironment () {
        Environment.DEFAULT.set(null);
    }

    private ComChannel getMockedComChannel() {
        ComChannel mock = mock(ComChannel.class);
        when(mock.getProperties()).thenReturn(TypedProperties.empty());
        return mock;
    }

    private MeterProtocol getMockedMeterProtocol() {
        return mock(MeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
    }

    @Test
    public void meterProtocolAdaptersNotNullTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertNotNull(meterProtocolAdapter.getMeterProtocolClockAdapter());
        assertNotNull(meterProtocolAdapter.getMeterProtocolLoadProfileAdapter());
        assertNotNull(meterProtocolAdapter.getMeterProtocolRegisterAdapter());
        assertNotNull(meterProtocolAdapter.getDeviceProtocolTopologyAdapter());
    }

    @Test
    public void dummyRegisterProtocolTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertTrue(meterProtocolAdapter.getMeterProtocolRegisterAdapter().getRegisterProtocol() instanceof DeviceRegisterReadingNotSupported);
    }

    @Test
    public void notTheDummyRegisterProtocolTest() {
        RegisterSupportedMeterProtocol meterProtocol = mock(RegisterSupportedMeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertFalse(meterProtocolAdapter.getMeterProtocolRegisterAdapter().getRegisterProtocol() instanceof DeviceRegisterReadingNotSupported);
        assertTrue(meterProtocolAdapter.getMeterProtocolRegisterAdapter().getRegisterProtocol() instanceof RegisterSupportedMeterProtocol);
    }

    /**
     * IOExceptions should be properly handled by the adapter
     *
     * @throws IOException if a direct call to {@link MeterProtocol#release()} is made
     */
    @Test(expected = LegacyProtocolException.class)
    public void terminateTest() throws IOException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        doThrow(new IOException("Could not terminate/release the protocol")).when(meterProtocol).release();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        meterProtocolAdapter.terminate();
    }

    @Test
    public void getRequiredKeysCorrectTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> requiredKeys = new ArrayList<>();
        requiredKeys.add(PropertySpecFactory.stringPropertySpec("r1"));
        requiredKeys.add(PropertySpecFactory.stringPropertySpec("r2"));
        requiredKeys.add(PropertySpecFactory.stringPropertySpec("r3"));
        when(meterProtocol.getRequiredProperties()).thenReturn(requiredKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(getRequiredPropertiesFromSet(meterProtocolAdapter.getPropertySpecs())).isEmpty(); // the optional properties are replaced by the hardcoded legacy values
    }

    private List<PropertySpec> getRequiredPropertiesFromSet(List<PropertySpec> propertySpecs) {
        List<PropertySpec> requiredProperties = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            if(propertySpec.isRequired()){
                requiredProperties.add(propertySpec);
            }
        }
        return requiredProperties;
    }

    @Test
    public void getOptionalKeysCorrectTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> optionalKeys = new ArrayList<>();
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o1"));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o2"));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o3"));
        when(meterProtocol.getOptionalProperties()).thenReturn(optionalKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        List<PropertySpec> optionalPropertiesFromSet = getOptionalPropertiesFromSet(meterProtocolAdapter.getPropertySpecs());
        assertThat(optionalPropertiesFromSet).isNotEmpty(); // the optional properties are replaced by the hardcoded legacy values
        assertThat(optionalPropertiesFromSet).hasSize(4);
        assertThat(optionalPropertiesFromSet).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                int count = 0;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(MeterProtocol.NODEID)) {
                        count |= 0b0001;
                    } else if (propertySpec.getName().equals(MeterProtocol.ADDRESS)) {
                        count |= 0b0010;
                    } else if (propertySpec.getName().equals("callHomeId")) {
                        count |= 0b0100;
                    } else if (propertySpec.getName().equals("deviceTimeZone")) {
                        count |= 0b1000;
                    } else {
                        count = -1;
                    }
                }
                return count == 0b1111;
            }
        });
    }

    private List<PropertySpec> getOptionalPropertiesFromSet(List<PropertySpec> propertySpecs) {
        List<PropertySpec> requiredProperties = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            if(!propertySpec.isRequired()){
                requiredProperties.add(propertySpec);
            }
        }
        return requiredProperties;
    }

    @Test
    public void getDeviceProtocolDialect() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> optionalKeys = new ArrayList<>();
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o1"));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o2"));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o3"));
        when(meterProtocol.getOptionalProperties()).thenReturn(optionalKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        final List<DeviceProtocolDialect> deviceProtocolDialects = meterProtocolAdapter.getDeviceProtocolDialects();

        // asserts
        assertThat(deviceProtocolDialects).hasSize(1);
        final DeviceProtocolDialect deviceProtocolDialect = deviceProtocolDialects.get(0);
        assertThat(getOptionalPropertiesFromSet(deviceProtocolDialect.getPropertySpecs())).contains(LegacyPropertySpecSupport.toPropertySpecs(optionalKeys, false).toArray(new PropertySpec[optionalKeys.size()]));
        assertThat(getRequiredPropertiesFromSet(deviceProtocolDialect.getPropertySpecs())).isEmpty();
    }

    @Test
    public void versionTest() {
        final String version = "ProtocolVersion";
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        when(meterProtocol.getProtocolVersion()).thenReturn(version);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertEquals(version, meterProtocolAdapter.getVersion());
    }

    @Test(expected = LegacyProtocolException.class)
    public void invalidPropertyAdditionTest() throws InvalidPropertyException, MissingPropertyException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        TypedProperties properties = TypedProperties.empty();
        doThrow(new InvalidPropertyException("Invalid property received")).when(meterProtocol).setProperties(Matchers.<Properties>any());
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());

        meterProtocolAdapter.copyProperties(properties); // should be successful
        meterProtocolAdapter.addDeviceProtocolDialectProperties(properties); // should fail
        meterProtocolAdapter.setSecurityPropertySet(null);
    }

    @Test(expected = LegacyProtocolException.class)
    public void missingPropertyExceptionTest() throws InvalidPropertyException, MissingPropertyException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        TypedProperties properties = TypedProperties.empty();
        doThrow(new MissingPropertyException("Missing property")).when(meterProtocol).setProperties(Matchers.<Properties>any());
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());

        meterProtocolAdapter.copyProperties(properties); // should be successful
        meterProtocolAdapter.addDeviceProtocolDialectProperties(properties); // should fail
        meterProtocolAdapter.setSecurityPropertySet(null);
    }

    @Test
    public void getSerialNumberTest() {
        final String meterSerialNumber = "MeterSerialNumber";
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(meterSerialNumber);
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertEquals(meterSerialNumber, meterProtocolAdapter.getSerialNumber());
    }

    @Test
    public void cachingProtocolTests() throws BusinessException, SQLException {
        final Object cacheObject = new BigDecimal("1256.6987");
        final int deviceId = 123;
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // Calling all business method on CachingProtocol
        meterProtocolAdapter.setCache(cacheObject);
        meterProtocolAdapter.getCache();
        meterProtocolAdapter.fetchCache(deviceId);
        meterProtocolAdapter.updateCache(deviceId, cacheObject);

        // Verify that the adapter properly forwarded the method calls to the meterProtocol
        verify(meterProtocol).setCache(cacheObject);
        verify(meterProtocol).getCache();
        verify(meterProtocol).fetchCache(deviceId);
        verify(meterProtocol).updateCache(deviceId, cacheObject);
    }

    @Test
    public void logOnWithoutViolationsTest() throws IOException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // call the logOn business method
        meterProtocolAdapter.logOn();

        // verify that the adapter properly called the connect() of the meterProtocol
        verify(meterProtocol).connect();
    }

    @Test
    public void logOnWithExceptionTest() throws CommunicationException, IOException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        doThrow(new IOException("Connection failed for a test reason")).when(meterProtocol).connect();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        try {
            // call the logOn business method
            meterProtocolAdapter.logOn();
        } catch (CommunicationException e) {
            if (!e.getMessageId().equals("CSC-COM-113")) {
                throw e;
            }
        }
    }

    @Test
    public void logOffWithoutViolationsTest() throws IOException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // call the business method
        meterProtocolAdapter.logOff();

        // verify that the adapter properly called the connect() of the meterProtocol
        verify(meterProtocol).disconnect();
    }

    @Test
    public void logOffWithExceptionTest() throws IOException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        doThrow(new IOException("Disconnect failed for a test reason")).when(meterProtocol).disconnect();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        try {
            // call the business method
            meterProtocolAdapter.logOff();
        } catch (CommunicationException e) {
            if (!e.getMessageId().equals("CSC-COM-114")) {
                throw e;
            }
        }
    }

    @Test
    public void enableHHUSignOnWithoutExceptionsTest() throws ConnectionException {
        HhuEnabledMeterProtocol meterProtocol = mock(HhuEnabledMeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // call the business method
        meterProtocolAdapter.enableHHUSignOn(any(SerialCommunicationChannel.class));

        // verify that the adapter properly called the method of the meterProtocol
        verify(meterProtocol).enableHHUSignOn(any(SerialCommunicationChannel.class));
    }

    @Test
    public void enableHHUSignOnWithoutExceptionsWithDataReadOut() throws ConnectionException {
        HhuEnabledMeterProtocol meterProtocol = mock(HhuEnabledMeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // call the business method
        meterProtocolAdapter.enableHHUSignOn(any(SerialCommunicationChannel.class), anyBoolean());

        // verify that the adapter properly called the method of the meterProtocol
        verify(meterProtocol).enableHHUSignOn(any(SerialCommunicationChannel.class), anyBoolean());
    }

    @Test
    public void getHHUDataReadoutTest() {
        HhuEnabledMeterProtocol meterProtocol = mock(HhuEnabledMeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // call the business method
        meterProtocolAdapter.getHHUDataReadout();

        // verify that the adapter properly called the method of the meterProtocol
        verify(meterProtocol).getHHUDataReadout();
    }

    @Test
    public void getHHUDataReadoutEmptyByteArray() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // call the business method
        byte[] hhuDataReadout = meterProtocolAdapter.getHHUDataReadout();

        // verify that we received an empty byteArray
        assertNotNull(hhuDataReadout);
        assertEquals(0, hhuDataReadout.length);
    }

    @Test
    public void getCapabilitiesTest() throws ClassNotFoundException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapter meterProtocolAdapter = spy(new MeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel));
        when(meterProtocolAdapter.getProtocolClass()).thenReturn(MockDeviceProtocol.class);

        // assert that the adapter provides all capabilities
        assertThat(meterProtocolAdapter.getDeviceProtocolCapabilities()).containsOnly(
                DeviceProtocolCapabilities.PROTOCOL_SESSION, DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Test
    public void testGetSecurityPropertiesWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        MeterProtocol adaptedProtocol = new SimpleTestMeterProtocol();
        MeterProtocolAdapter adapter = new TestMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        List<PropertySpec> securityProperties = adapter.getSecurityProperties();

        // Asserts
        assertThat(securityProperties).isEqualTo(new SimpleTestDeviceSecuritySupport().getSecurityProperties());
    }

    @Test
    public void testGetSecurityPropertiesWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        MeterProtocolAdapter adapter = new MeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // Business method
        adapter.getSecurityProperties();

        // Asserts
        verify(adaptedProtocol).getSecurityProperties();
    }

    @Test
    public void testGetSecurityPropertySpecWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        MeterProtocol adaptedProtocol = new SimpleTestMeterProtocol();
        MeterProtocolAdapter adapter = new TestMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        PropertySpec whatEverPropertySpec = adapter.getSecurityPropertySpec(PROPERTY_SPEC_NAME);
        PropertySpec firstPropertySpec = adapter.getSecurityPropertySpec(SimpleTestDeviceSecuritySupport.FIRST_PROPERTY_NAME);

        // Asserts
        assertThat(whatEverPropertySpec).isNull();
        assertThat(firstPropertySpec).isEqualTo(OptionalPropertySpecFactory.newInstance().stringPropertySpec(SimpleTestDeviceSecuritySupport.FIRST_PROPERTY_NAME));
    }

    @Test
    public void testGetSecurityPropertySpecWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        MeterProtocolAdapter adapter = new MeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // Business method
        adapter.getSecurityPropertySpec(PROPERTY_SPEC_NAME);

        // Asserts
        verify(adaptedProtocol).getSecurityPropertySpec(PROPERTY_SPEC_NAME);
    }

    @Test
    public void testGetAuthenticationAccessLevelsWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        MeterProtocol adaptedProtocol = new SimpleTestMeterProtocol();
        MeterProtocolAdapter adapter = new TestMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = adapter.getAuthenticationAccessLevels();

        // Asserts
        assertThat(authenticationAccessLevels).hasSize(1);
        assertThat(authenticationAccessLevels.get(0).getId()).isEqualTo(SimpleTestDeviceSecuritySupport.AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID);
    }

    @Test
    public void testGetAuthenticationAccessLevelsWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        MeterProtocolAdapter adapter = new MeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // Business method
        adapter.getAuthenticationAccessLevels();

        // Asserts
        verify(adaptedProtocol).getAuthenticationAccessLevels();
    }

    @Test
    public void testGetEncryptionAccessLevelsWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        MeterProtocol adaptedProtocol = new SimpleTestMeterProtocol();
        MeterProtocolAdapter adapter = new TestMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        List<EncryptionDeviceAccessLevel> encryptionAccessLevels = adapter.getEncryptionAccessLevels();

        // Asserts
        assertThat(encryptionAccessLevels).hasSize(1);
        assertThat(encryptionAccessLevels.get(0).getId()).isEqualTo(SimpleTestDeviceSecuritySupport.ENCRYPTION_DEVICE_ACCESS_LEVEL_ID);
    }

    @Test
    public void testGetEncryptionAccessLevelsWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        MeterProtocolAdapter adapter = new MeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // Business method
        adapter.getEncryptionAccessLevels();

        // Asserts
        verify(adaptedProtocol).getEncryptionAccessLevels();
    }

    @Test
    public void testGetSecurityRelationTypeNameWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        MeterProtocol adaptedProtocol = new SimpleTestMeterProtocol();
        MeterProtocolAdapter adapter = new TestMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Asserts
        assertThat(SimpleTestDeviceSecuritySupport.DUMMY_RELATION_TYPE_NAME).isEqualTo(adapter.getSecurityRelationTypeName());
    }

    @Test
    public void testGetSecurityRelationTypeNameWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        MeterProtocolAdapter adapter = new MeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.dataModel);

        // Business method
        adapter.getSecurityRelationTypeName();

        // Asserts
        verify(adaptedProtocol).getSecurityRelationTypeName();
    }

    private interface MeterProtocolWithDeviceSecuritySupport extends MeterProtocol, DeviceSecuritySupport {

    }

    private class TestMeterProtocolAdapter extends MeterProtocolAdapter {

        private TestMeterProtocolAdapter(final MeterProtocol meterProtocol, ProtocolPluggableService protocolPluggableService, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory) {
            super(meterProtocol, protocolPluggableService, securitySupportAdapterMappingFactory, dataModel);
        }

        @Override
        protected void initializeAdapters() {
            setMeterProtocolSecuritySupportAdapter(new MeterProtocolSecuritySupportAdapter(getMeterProtocol(), mock(PropertiesAdapter.class), this.getSecuritySupportAdapterMappingFactory()));
        }
    }

}