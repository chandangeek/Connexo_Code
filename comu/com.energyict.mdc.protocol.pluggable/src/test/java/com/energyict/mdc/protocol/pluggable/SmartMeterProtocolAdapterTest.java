package com.energyict.mdc.protocol.pluggable;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.TimeZoneFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.BasicPropertySpec;
import com.energyict.mdc.dynamic.impl.PropertySpecBuilderImpl;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceCapabilityAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock.HhuEnabledSmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SimpleTestSmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolSecuritySupportAdapter;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import org.fest.assertions.api.Assertions;
import org.fest.assertions.core.Condition;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for the {@link SmartMeterProtocolAdapter}.
 *
 * @author gna
 * @since 5/04/12 - 13:20
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartMeterProtocolAdapterTest {

    private static final String PROPERTY_SPEC_NAME = "whatever";

    @Mock
    private SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;
    @Mock
    private MessageAdapterMappingFactory messageAdapterMappingFactory;
    @Mock
    private CapabilityAdapterMappingFactory capabilityAdapterMappingFactory;
    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private MeteringService meteringService;

    private InMemoryPersistence inMemoryPersistence;
    private ProtocolPluggableServiceImpl protocolPluggableService;

    @Before
    public void initializeDatabaseAndMocks() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase(
                "SmartMeterProtocolAdapterTest.mdc.protocol.pluggable",
                dataModel -> dataModel.persist(new DeviceCapabilityAdapterMappingImpl(MockDeviceProtocol.class.getCanonicalName(), 6)));
        this.protocolPluggableService = this.inMemoryPersistence.getProtocolPluggableService();
        this.initializeMocks(this.protocolPluggableService);
    }

    private void initializeMocks(ProtocolPluggableServiceImpl protocolPluggableService) {
        protocolPluggableService.addCollectedDataFactory(this.collectedDataFactory);
        DeviceProtocolSecurityService deviceProtocolSecurityService = this.inMemoryPersistence.getDeviceProtocolSecurityService();
        PropertySpecService propertySpecService = inMemoryPersistence.getPropertySpecService();
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecuritySupport.FIRST_PROPERTY_NAME, false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecuritySupport.FIRST_PROPERTY_NAME, false, new StringFactory()));
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecuritySupport.SECOND_PROPERTY_NAME, false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecuritySupport.SECOND_PROPERTY_NAME, false, new StringFactory()));
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecuritySupport.THIRD_PROPERTY_NAME, false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecuritySupport.THIRD_PROPERTY_NAME, false, new StringFactory()));
        when(propertySpecService.timeZonePropertySpec(anyString(), anyBoolean(), any()))
                .thenAnswer(invocationOnMock -> {
                    String name = (String) invocationOnMock.getArguments()[0];
                    boolean required = ((Boolean) invocationOnMock.getArguments()[1]);
                    TimeZone defaultValue = (TimeZone) invocationOnMock.getArguments()[2];
                    TimeZone[] possibleValues = {
                            TimeZone.getTimeZone("GMT"),
                            TimeZone.getTimeZone("Europe/Brussels"),
                            TimeZone.getTimeZone("EST"),
                            TimeZone.getTimeZone("Europe/Moscow")};
                    PropertySpecBuilder timeZonePropertySpecBuilder = PropertySpecBuilderImpl
                            .forClass(new TimeZoneFactory()).name(name).setDefaultValue(defaultValue).markExhaustive().addValues(possibleValues);
                    if (required) {
                        timeZonePropertySpecBuilder.markRequired();
                    }
                    return timeZonePropertySpecBuilder.finish();
                });   SimpleTestDeviceSecuritySupport securitySupport = new SimpleTestDeviceSecuritySupport(propertySpecService);
        when(deviceProtocolSecurityService.createDeviceProtocolSecurityFor(SimpleTestDeviceSecuritySupport.class.getName()))
            .thenReturn(securitySupport);

        when(this.securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol
                (SimpleTestSmartMeterProtocol.class.getName())).thenReturn(SimpleTestDeviceSecuritySupport.class.getName());

        IdBusinessObjectFactory timeZoneInUseFactory = mock(IdBusinessObjectFactory.class);
        when(timeZoneInUseFactory.getInstanceType()).thenReturn(TimeZone.class);
        this.mockPropertySpecs();
    }

    private void mockPropertySpecs() {
        PropertySpec nodeIdPropertySpec = mock(PropertySpec.class);
        when(nodeIdPropertySpec.isRequired()).thenReturn(false);
        when(nodeIdPropertySpec.getName()).thenReturn(MeterProtocol.NODEID);
        when(nodeIdPropertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(inMemoryPersistence.getPropertySpecService().
                basicPropertySpec(eq(MeterProtocol.NODEID), eq(false), any(ValueFactory.class))).
                thenReturn(nodeIdPropertySpec);
        PropertySpec addressPropertySpec = mock(PropertySpec.class);
        when(addressPropertySpec.isRequired()).thenReturn(false);
        when(addressPropertySpec.getName()).thenReturn(MeterProtocol.ADDRESS);
        when(addressPropertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(inMemoryPersistence.getPropertySpecService().
                basicPropertySpec(eq(MeterProtocol.ADDRESS), eq(false), any(ValueFactory.class))).
                thenReturn(addressPropertySpec);
        PropertySpec callHomeIdPropertySpec = mock(PropertySpec.class);
        when(callHomeIdPropertySpec.isRequired()).thenReturn(false);
        when(callHomeIdPropertySpec.getName()).thenReturn(DeviceProtocolProperty.callHomeId.name());
        when(callHomeIdPropertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(inMemoryPersistence.getPropertySpecService().
                basicPropertySpec(eq(DeviceProtocolProperty.callHomeId.name()), eq(false), any(ValueFactory.class))).
                thenReturn(callHomeIdPropertySpec);
        PropertySpec deviceTimeZonePropertySpec = mock(PropertySpec.class);
        when(deviceTimeZonePropertySpec.isRequired()).thenReturn(false);
        when(deviceTimeZonePropertySpec.getName()).thenReturn(DeviceProtocolProperty.deviceTimeZone.name());
        when(deviceTimeZonePropertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(inMemoryPersistence.getPropertySpecService().
                basicPropertySpec(eq(DeviceProtocolProperty.deviceTimeZone.name()), eq(false), any(ValueFactory.class))).
                thenReturn(deviceTimeZonePropertySpec);
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    public ComChannel getMockedComChannel() {
        ComChannel mock = mock(ComChannel.class);
        when(mock.getProperties()).thenReturn(TypedProperties.empty());
        return mock;
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
    public void smartMeterProtocolAdaptersNotNull() {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertNotNull(smartMeterProtocolAdapter.getSmartMeterProtocolClockAdapter());
        assertNotNull(smartMeterProtocolAdapter.getSmartMeterProtocolLoadProfileAdapter());
        assertNotNull(smartMeterProtocolAdapter.getDeviceProtocolTopologyAdapter());
        assertNotNull(smartMeterProtocolAdapter.getSmartMeterProtocolLogBookAdapter());
        assertNotNull(smartMeterProtocolAdapter.getSmartMeterProtocolRegisterAdapter());
    }

    @Test(expected = LegacyProtocolException.class)
    public void terminateExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        doThrow(new IOException("Could not terminate/release the protocol")).when(smartMeterProtocol).release();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        smartMeterProtocolAdapter.terminate();
    }

    @Test
    public void getSerialNumberTest() throws IOException {
        final String meterSerialNumber = "MeterSerialNumber";
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getMeterSerialNumber()).thenReturn(meterSerialNumber);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertEquals(meterSerialNumber, smartMeterProtocolAdapter.getSerialNumber());
    }

    @Test(expected = LegacyProtocolException.class)
    public void getSerialNumberExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getMeterSerialNumber()).thenThrow(new IOException("Could not fetch the serialnumber"));
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        smartMeterProtocolAdapter.getSerialNumber();
    }

    @Test
    public void getRequiredKeysCorrectTest() {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> requiredKeys = new ArrayList<>();
        requiredKeys.add(PropertySpecFactory.stringPropertySpec("r1"));
        requiredKeys.add(PropertySpecFactory.stringPropertySpec("r2"));
        requiredKeys.add(PropertySpecFactory.stringPropertySpec("r3"));
        when(smartMeterProtocol.getRequiredProperties()).thenReturn(requiredKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(getRequiredPropertiesFromSet(smartMeterProtocolAdapter.getPropertySpecs())).isEmpty(); // the optional properties are replaced by the hardcoded legacy values
        assertThat(getOptionalPropertiesFromSet(smartMeterProtocolAdapter.getPropertySpecs())).isNotEmpty(); // the optional properties are replaced by the hardcoded legacy values
    }

    @Test
    public void getOptionalKeysCorrectTest() {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> optionalKeys = new ArrayList<>();
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o1"));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o2"));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o3"));
        when(smartMeterProtocol.getOptionalProperties()).thenReturn(optionalKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(getRequiredPropertiesFromSet(smartMeterProtocolAdapter.getPropertySpecs())).isEmpty(); // the optional properties are replaced by the hardcoded legacy values
        assertThat(getOptionalPropertiesFromSet(smartMeterProtocolAdapter.getPropertySpecs())).isNotEmpty(); // the optional properties are replaced by the hardcoded legacy values
        assertThat(smartMeterProtocolAdapter.getPropertySpecs()).hasSize(4);
        assertThat(smartMeterProtocolAdapter.getPropertySpecs()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                int count = 0;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.getName().equals(MeterProtocol.NODEID)) {
                        count |= 0b0001;
                    } else if (propertySpec.getName().equals(MeterProtocol.ADDRESS)) {
                        count |= 0b0010;
                    } else if (propertySpec.getName().equals(DeviceProtocolProperty.callHomeId.name())) {
                        count |= 0b0100;
                    } else if (propertySpec.getName().equals(DeviceProtocolProperty.deviceTimeZone.name())) {
                        count |= 0b1000;
                    } else {
                        count = -1;
                    }
                }
                return count == 0b1111;
            }
        });
    }

    @Test
    public void getDeviceProtocolDialect() {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> optionalKeys = new ArrayList<>();
        final List<String> optionalKeyNames = Arrays.asList("o1", "o2", "o3");
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o1"));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o2"));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o3"));
        when(smartMeterProtocol.getOptionalProperties()).thenReturn(optionalKeys);
        PropertySpecService propertySpecService = this.inMemoryPersistence.getPropertySpecService();
        doReturn(new BasicPropertySpec("o1", false, new StringFactory()))
            .when(propertySpecService).basicPropertySpec(eq("o1"), eq(false), any(ValueFactory.class));
        doReturn(new BasicPropertySpec("o2", false, new StringFactory()))
            .when(propertySpecService).basicPropertySpec(eq("o2"), eq(false), any(ValueFactory.class));
        doReturn(new BasicPropertySpec("o3", false, new StringFactory()))
            .when(propertySpecService).basicPropertySpec(eq("o3"), eq(false), any(ValueFactory.class));
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        final List<DeviceProtocolDialect> deviceProtocolDialects = smartMeterProtocolAdapter.getDeviceProtocolDialects();

        // asserts
        assertThat(deviceProtocolDialects).hasSize(1);
        final DeviceProtocolDialect deviceProtocolDialect = deviceProtocolDialects.get(0);
        assertThat(getOptionalPropertiesFromSet(deviceProtocolDialect.getPropertySpecs())).
                areExactly(optionalKeys.size(), new Condition<PropertySpec>() {
                    @Override
                    public boolean matches(PropertySpec propertySpec) {
                        return optionalKeyNames.contains(propertySpec.getName());
                    }
                });
        assertThat(getRequiredPropertiesFromSet(deviceProtocolDialect.getPropertySpecs())).isEmpty();
    }

    @Test
    public void getVersionTest() {
        final String version = "SmartMeterProtocolVersion";
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getVersion()).thenReturn(version);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertEquals(version, smartMeterProtocolAdapter.getVersion());
    }

    @Test
    public void cachingProtocolTests() throws BusinessException, SQLException {
        final Object cacheObject = new BigDecimal("1256.6987");
        final int deviceId = 123;
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // Calling all business method on CachingProtocol
        smartMeterProtocolAdapter.setCache(cacheObject);
        smartMeterProtocolAdapter.getCache();
        smartMeterProtocolAdapter.fetchCache(deviceId);
        smartMeterProtocolAdapter.updateCache(deviceId, cacheObject);

        // Verify that the adapter properly forwarded the method calls to the smartMeterProtocol
        verify(smartMeterProtocol).setCache(cacheObject);
        verify(smartMeterProtocol).getCache();
        verify(smartMeterProtocol).fetchCache(deviceId);
        verify(smartMeterProtocol).updateCache(deviceId, cacheObject);
    }

    @Test
    public void logOnWithoutViolationsTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the logOn business method
        smartMeterProtocolAdapter.logOn();

        // verify that the adapter properly called the logOn() of the smartMeterProtocol
        verify(smartMeterProtocol).connect();
    }

    @Test
    public void logOnWithExceptionTest() throws IOException, CommunicationException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        doThrow(new IOException("LogOn failed for a test reason")).when(smartMeterProtocol).connect();
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        try {
            // call the logOn business method
            smartMeterProtocolAdapter.logOn();
        } catch (CommunicationException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.PROTOCOL_CONNECT)) {
                throw e;
            }
        }
    }

    private SmartMeterProtocol getMockedSmartMeterProtocol() {
        return mock(SmartMeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
    }

    @Test
    public void logOffWithoutViolationsTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the logOn business method
        smartMeterProtocolAdapter.logOff();

        // verify that the adapter properly called the logOn() of the smartMeterProtocol
        verify(smartMeterProtocol).disconnect();
    }

    @Test
    public void logOffWithExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        doThrow(new IOException("LogOff failed for a test reason")).when(smartMeterProtocol).disconnect();
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        try {
            // call the logOn business method
            smartMeterProtocolAdapter.logOff();
        } catch (CommunicationException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.PROTOCOL_DISCONNECT)) {
                throw e;
            }
        }
    }


    @Test
    public void enableHHUSignOnWithoutExceptionsTest() throws ConnectionException {
        HhuEnabledSmartMeterProtocol smartMeterProtocol = getMockedHHUEnabledSmartMeterProtocol();
        SmartMeterProtocolAdapter meterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the business method
        meterProtocolAdapter.enableHHUSignOn(any(SerialCommunicationChannel.class));

        // verify that the adapter properly called the method of the smartMeterProtocol
        verify(smartMeterProtocol).enableHHUSignOn(any(SerialCommunicationChannel.class));
    }

    @Test
    public void enableHHUSignOnWithoutExceptionsWithDataReadOut() throws ConnectionException {
        HhuEnabledSmartMeterProtocol smartMeterProtocol = getMockedHHUEnabledSmartMeterProtocol();
        SmartMeterProtocolAdapter meterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the business method
        meterProtocolAdapter.enableHHUSignOn(any(SerialCommunicationChannel.class), anyBoolean());

        // verify that the adapter properly called the method of the smartMeterProtocol
        verify(smartMeterProtocol).enableHHUSignOn(any(SerialCommunicationChannel.class), anyBoolean());
    }

    @Test
    public void getHHUDataReadoutTest() {
        HhuEnabledSmartMeterProtocol smartMeterProtocol = getMockedHHUEnabledSmartMeterProtocol();
        SmartMeterProtocolAdapter meterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the business method
        meterProtocolAdapter.getHHUDataReadout();

        // verify that the adapter properly called the method of the smartMeterProtocol
        verify(smartMeterProtocol).getHHUDataReadout();
    }

    private HhuEnabledSmartMeterProtocol getMockedHHUEnabledSmartMeterProtocol() {
        return mock(HhuEnabledSmartMeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
    }

    @Test
    public void getHHUDataReadoutEmptyByteArray() {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        SmartMeterProtocolAdapter meterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the business method
        byte[] hhuDataReadout = meterProtocolAdapter.getHHUDataReadout();

        // verify that we received an empty byteArray
        assertNotNull(hhuDataReadout);
        assertEquals(0, hhuDataReadout.length);
    }

    @Test
    public void getCapabilitiesTest() throws ClassNotFoundException {
        SmartMeterProtocol meterProtocol = getMockedSmartMeterProtocol();
        SmartMeterProtocolAdapter meterProtocolAdapter = spy(newSmartMeterProtocolAdapter(meterProtocol));
        when(meterProtocolAdapter.getProtocolClass()).thenReturn(MockDeviceProtocol.class);

        // assert that the adapter provides all capabilities
        assertThat(meterProtocolAdapter.getDeviceProtocolCapabilities()).containsOnly(
                DeviceProtocolCapabilities.PROTOCOL_SESSION, DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Test
    public void testGetSecurityPropertiesWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapter adapter =
                new TestSmartMeterProtocolAdapter(
                    adaptedProtocol,
                    this.inMemoryPersistence.getPropertySpecService(),
                    this.protocolPluggableService,
                    this.securitySupportAdapterMappingFactory,
                    this.capabilityAdapterMappingFactory,
                    this.messageAdapterMappingFactory,
                    this.collectedDataFactory);

        // Business method
        List<PropertySpec> securityProperties = adapter.getSecurityPropertySpecs();

        // Asserts
        assertThat(securityProperties).isEqualTo(new SimpleTestDeviceSecuritySupport(inMemoryPersistence.getPropertySpecService()).getSecurityPropertySpecs());
    }

    @Test
    public void testGetSecurityPropertiesWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        SmartMeterProtocolAdapter adapter = newSmartMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getSecurityPropertySpecs();

        // Asserts
        verify(adaptedProtocol).getSecurityPropertySpecs();
    }

    @Test
    public void testGetSecurityPropertySpecWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapter adapter =
                new TestSmartMeterProtocolAdapter(
                        adaptedProtocol,
                        this.inMemoryPersistence.getPropertySpecService(),
                        this.protocolPluggableService,
                        this.securitySupportAdapterMappingFactory,
                        this.capabilityAdapterMappingFactory,
                        this.messageAdapterMappingFactory,
                        this.collectedDataFactory);

        // Business method
        PropertySpec whatEverPropertySpec = adapter.getSecurityPropertySpec(PROPERTY_SPEC_NAME);
        PropertySpec firstPropertySpec = adapter.getSecurityPropertySpec(SimpleTestDeviceSecuritySupport.FIRST_PROPERTY_NAME);

        // Asserts
        assertThat(whatEverPropertySpec).isNull();
        assertThat(firstPropertySpec).isEqualTo(new PropertySpecServiceImpl().basicPropertySpec(SimpleTestDeviceSecuritySupport.FIRST_PROPERTY_NAME, false, new StringFactory()));
    }

    @Test
    public void testGetSecurityPropertySpecWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        SmartMeterProtocolAdapter adapter = newSmartMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getSecurityPropertySpec(PROPERTY_SPEC_NAME);

        // Asserts
        verify(adaptedProtocol).getSecurityPropertySpec(PROPERTY_SPEC_NAME);
    }

    @Test
    public void testGetAuthenticationAccessLevelsWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapter adapter =
                new TestSmartMeterProtocolAdapter(
                        adaptedProtocol,
                        this.inMemoryPersistence.getPropertySpecService(),
                        this.protocolPluggableService,
                        this.securitySupportAdapterMappingFactory,
                        this.capabilityAdapterMappingFactory,
                        this.messageAdapterMappingFactory,
                        this.collectedDataFactory);

        // Business method
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = adapter.getAuthenticationAccessLevels();

        // Asserts
        assertThat(authenticationAccessLevels).hasSize(1);
        assertThat(authenticationAccessLevels.get(0).getId()).isEqualTo(SimpleTestDeviceSecuritySupport.AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID);
    }

    @Test
    public void testGetAuthenticationAccessLevelsWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        SmartMeterProtocolAdapter adapter = newSmartMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getAuthenticationAccessLevels();

        // Asserts
        verify(adaptedProtocol).getAuthenticationAccessLevels();
    }

    @Test
    public void testGetEncryptionAccessLevelsWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapter adapter =
                new TestSmartMeterProtocolAdapter(
                        adaptedProtocol,
                        this.inMemoryPersistence.getPropertySpecService(),
                        this.protocolPluggableService,
                        this.securitySupportAdapterMappingFactory,
                        this.capabilityAdapterMappingFactory,
                        this.messageAdapterMappingFactory,
                        this.collectedDataFactory);

        // Business method
        List<EncryptionDeviceAccessLevel> encryptionAccessLevels = adapter.getEncryptionAccessLevels();

        // Asserts
        assertThat(encryptionAccessLevels).hasSize(1);
        assertThat(encryptionAccessLevels.get(0).getId()).isEqualTo(SimpleTestDeviceSecuritySupport.ENCRYPTION_DEVICE_ACCESS_LEVEL_ID);
    }

    @Test
    public void testGetEncryptionAccessLevelsWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        SmartMeterProtocolAdapter adapter = newSmartMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getEncryptionAccessLevels();

        // Asserts
        verify(adaptedProtocol).getEncryptionAccessLevels();
    }

    @Test
    public void testGetSecurityRelationTypeNameWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapter adapter =
                new TestSmartMeterProtocolAdapter(
                        adaptedProtocol,
                        this.inMemoryPersistence.getPropertySpecService(),
                        this.protocolPluggableService,
                        this.securitySupportAdapterMappingFactory,
                        this.capabilityAdapterMappingFactory,
                        this.messageAdapterMappingFactory,
                        this.collectedDataFactory);

        // Asserts
        Assertions.assertThat(SimpleTestDeviceSecuritySupport.DUMMY_RELATION_TYPE_NAME).isEqualTo(adapter.getSecurityRelationTypeName());
    }

    @Test
    public void testGetSecurityRelationTypeNameWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        SmartMeterProtocolAdapter adapter = newSmartMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getSecurityRelationTypeName();

        // Asserts
        verify(adaptedProtocol).getSecurityRelationTypeName();
    }

    @Test
    public void getFirmwareVersionTest() throws IOException {
        String myTestFirmwareVersion = "jslmjksdfjjL1321";
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getFirmwareVersion()).thenReturn(myTestFirmwareVersion);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(offlineDevice.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());

        CollectedFirmwareVersion collectedFirmwareVersion = mock(CollectedFirmwareVersion.class);
        when(collectedDataFactory.createFirmwareVersionsCollectedData(deviceIdentifier)).thenReturn(collectedFirmwareVersion);
        CollectedFirmwareVersion firmwareVersions = smartMeterProtocolAdapter.getFirmwareVersions();

        verify(collectedFirmwareVersion).setActiveMeterFirmwareVersion(myTestFirmwareVersion);
    }

    @Test
    public void getFirmwareVersionLegacyExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        IOException expectedException = createGetFirmwareVersionIOException();
        when(smartMeterProtocol.getFirmwareVersion()).thenThrow(expectedException);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(offlineDevice.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());

        CollectedFirmwareVersion collectedFirmwareVersion = mock(CollectedFirmwareVersion.class);
        when(collectedDataFactory.createFirmwareVersionsCollectedData(deviceIdentifier)).thenReturn(collectedFirmwareVersion);
        try {
            CollectedFirmwareVersion firmwareVersions = smartMeterProtocolAdapter.getFirmwareVersions();
            fail("Shoud not get here!");
        } catch (LegacyProtocolException e) {
            assertThat(e.getCause()).isEqualTo(expectedException);
        }
    }

    private IOException createGetFirmwareVersionIOException() {
        return new IOException("MyExpectedIoException");
    }

    protected SmartMeterProtocolAdapter newSmartMeterProtocolAdapter(SmartMeterProtocol smartMeterProtocol) {
        DataModel dataModel = this.protocolPluggableService.getDataModel();
        return new SmartMeterProtocolAdapter(
                smartMeterProtocol,
                this.inMemoryPersistence.getPropertySpecService(),
                this.protocolPluggableService,
                this.securitySupportAdapterMappingFactory,
                new CapabilityAdapterMappingFactoryImpl(dataModel),
                messageAdapterMappingFactory, dataModel,
                this.inMemoryPersistence.getIssueService(),
                collectedDataFactory,
                meteringService);
    }

    private interface MeterProtocolWithDeviceSecuritySupport extends SmartMeterProtocol, DeviceSecuritySupport {
    }

    private class TestSmartMeterProtocolAdapter extends SmartMeterProtocolAdapter {

        private TestSmartMeterProtocolAdapter(SmartMeterProtocol meterProtocol, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService1, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, CapabilityAdapterMappingFactory capabilityAdapterMappingFactory, MessageAdapterMappingFactory messageAdapterMappingFactory, CollectedDataFactory collectedDataFactory) {
            super(meterProtocol, propertySpecService, protocolPluggableService1, securitySupportAdapterMappingFactory, capabilityAdapterMappingFactory, messageAdapterMappingFactory, protocolPluggableService.getDataModel(), inMemoryPersistence.getIssueService(), collectedDataFactory, meteringService);
        }

        @Override
        protected void initializeAdapters() {
            setSmartMeterProtocolSecuritySupportAdapter(
                    new SmartMeterProtocolSecuritySupportAdapter(
                            getSmartMeterProtocol(),
                            this.getPropertySpecService(),
                            protocolPluggableService,
                            mock(PropertiesAdapter.class),
                            this.getSecuritySupportAdapterMappingFactory()));
        }

        @Override
        public DeviceFunction getDeviceFunction() {
            return null;
        }

        @Override
        public ManufacturerInformation getManufacturerInformation() {
            return null;
        }
    }

}