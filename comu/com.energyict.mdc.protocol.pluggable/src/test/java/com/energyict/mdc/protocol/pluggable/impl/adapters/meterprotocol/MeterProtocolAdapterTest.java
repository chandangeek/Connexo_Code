package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.TimeZoneFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.BasicPropertySpec;
import com.energyict.mdc.dynamic.impl.PropertySpecBuilderImpl;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceCapabilityAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceRegisterReadingNotSupported;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecurityProperties;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock.HhuEnabledMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock.RegisterSupportedMeterProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
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
    private static final String PROTOCOL_CLASS = "com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.SimpleTestMeterProtocol";

    @Mock
    private MessageAdapterMappingFactory messageAdapterMappingFactory;
    @Mock
    private CapabilityAdapterMappingFactory capabilityAdapterMappingFactory;
    @Mock
    private SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;
    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private MeteringService meteringService;
    @Mock
    private Thesaurus thesaurus;

    private InMemoryPersistence inMemoryPersistence;
    private ProtocolPluggableServiceImpl protocolPluggableService;

    @Before
    public void initializeDatabaseAndMocks() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase("MeterProtocolAdapterTest.mdc.protocol.pluggable");
        this.protocolPluggableService = this.inMemoryPersistence.getProtocolPluggableService();
        this.initializeMocks(this.protocolPluggableService);
    }

    private void initializeMocks(ProtocolPluggableServiceImpl protocolPluggableService) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        protocolPluggableService.addCollectedDataFactory(this.collectedDataFactory);

        when(securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol(PROTOCOL_CLASS)).
                thenReturn("com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport");
        when(capabilityAdapterMappingFactory.getCapabilitiesMappingForDeviceProtocol(MockDeviceProtocol.class.getCanonicalName())).thenReturn(6);  //6 = master and session capability
        PropertySpecService propertySpecService = inMemoryPersistence.getPropertySpecService();
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName(), false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName(), false, new StringFactory()));
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.SECOND.javaName(), false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.SECOND.javaName(), false, new StringFactory()));
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.THIRD.javaName(), false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.THIRD.javaName(), false, new StringFactory()));
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
                });

        SimpleTestDeviceSecuritySupport securitySupport = new SimpleTestDeviceSecuritySupport(propertySpecService);
        DeviceProtocolSecurityService deviceProtocolSecurityService = this.inMemoryPersistence.getDeviceProtocolSecurityService();
        when(deviceProtocolSecurityService.createDeviceProtocolSecurityFor("com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport")).
                thenReturn(securitySupport);
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
        when(callHomeIdPropertySpec.getName()).thenReturn(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName());
        when(callHomeIdPropertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(inMemoryPersistence.getPropertySpecService().
                basicPropertySpec(eq(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName()), eq(false), any(ValueFactory.class))).
                thenReturn(callHomeIdPropertySpec);
        PropertySpec deviceTimeZonePropertySpec = mock(PropertySpec.class);
        when(deviceTimeZonePropertySpec.isRequired()).thenReturn(false);
        when(deviceTimeZonePropertySpec.getName()).thenReturn(DeviceProtocolProperty.DEVICE_TIME_ZONE.javaFieldName());
        when(deviceTimeZonePropertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(inMemoryPersistence.getPropertySpecService().
                basicPropertySpec(eq(DeviceProtocolProperty.DEVICE_TIME_ZONE.javaFieldName()), eq(false), any(ValueFactory.class))).
                thenReturn(deviceTimeZonePropertySpec);
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
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
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(meterProtocolAdapter.getMeterProtocolClockAdapter()).isNotNull();
        assertThat(meterProtocolAdapter.getMeterProtocolLoadProfileAdapter()).isNotNull();
        assertThat(meterProtocolAdapter.getMeterProtocolRegisterAdapter()).isNotNull();
        assertThat(meterProtocolAdapter.getDeviceProtocolTopologyAdapter()).isNotNull();
    }

    @Test
    public void dummyRegisterProtocolTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(meterProtocolAdapter.getMeterProtocolRegisterAdapter().getRegisterProtocol()).isInstanceOf(DeviceRegisterReadingNotSupported.class);
    }

    @Test
    public void notTheDummyRegisterProtocolTest() {
        RegisterSupportedMeterProtocol meterProtocol = mock(RegisterSupportedMeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(meterProtocolAdapter.getMeterProtocolRegisterAdapter().getRegisterProtocol()).isNotInstanceOf(DeviceRegisterReadingNotSupported.class);
        assertThat(meterProtocolAdapter.getMeterProtocolRegisterAdapter().getRegisterProtocol()).isInstanceOf(RegisterSupportedMeterProtocol.class);
    }

    /**
     * Tests that IOExceptions are properly handled by the adapter.
     *
     * @throws IOException if a direct call to {@link MeterProtocol#release()} is made
     */
    @Test(expected = LegacyProtocolException.class)
    public void terminateTest() throws IOException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        doThrow(new IOException("Could not terminate/release the protocol")).when(meterProtocol).release();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        meterProtocolAdapter.terminate();
    }

    @Test
    public void getRequiredKeysCorrectTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        List<PropertySpec> requiredKeys = new ArrayList<>();
        requiredKeys.add(this.newStringPropertySpec("r1"));
        requiredKeys.add(this.newStringPropertySpec("r2"));
        requiredKeys.add(this.newStringPropertySpec("r3"));
        when(meterProtocol.getRequiredProperties()).thenReturn(requiredKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(getRequiredPropertiesFromSet(meterProtocolAdapter.getPropertySpecs())).isEmpty(); // the optional properties are replaced by the hardcoded legacy values
    }

    private PropertySpec newStringPropertySpec(String name) {
        return new PropertySpecServiceImpl().basicPropertySpec(name, false, new StringFactory());
    }

    private List<PropertySpec> getRequiredPropertiesFromSet(List<PropertySpec> propertySpecs) {
        return propertySpecs
                .stream()
                .filter(PropertySpec::isRequired)
                .collect(Collectors.toList());
    }

    @Test
    public void getOptionalKeysCorrectTest() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        List<PropertySpec> optionalKeys = new ArrayList<>();
        optionalKeys.add(this.newStringPropertySpec("o1"));
        optionalKeys.add(this.newStringPropertySpec("o2"));
        optionalKeys.add(this.newStringPropertySpec("o3"));
        when(meterProtocol.getOptionalProperties()).thenReturn(optionalKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        List<PropertySpec> optionalPropertiesFromSet = getOptionalPropertiesFromSet(meterProtocolAdapter.getPropertySpecs());
        assertThat(optionalPropertiesFromSet).hasSize(7);
    }

    private List<PropertySpec> getOptionalPropertiesFromSet(List<PropertySpec> propertySpecs) {
        return propertySpecs.stream().filter(propertySpec -> !propertySpec.isRequired()).collect(Collectors.toList());
    }

    @Test
    public void getDeviceProtocolDialect() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        List<PropertySpec> optionalKeys = new ArrayList<>();
        final List<String> optionalKeyNames = Arrays.asList("o1", "o2", "o3");
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o1", inMemoryPersistence.getPropertySpecService()));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o2", inMemoryPersistence.getPropertySpecService()));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o3", inMemoryPersistence.getPropertySpecService()));
        when(meterProtocol.getOptionalProperties()).thenReturn(optionalKeys);
        PropertySpecService propertySpecService = this.inMemoryPersistence.getPropertySpecService();
        doReturn(new BasicPropertySpec("o1", false, new StringFactory()))
                .when(propertySpecService).basicPropertySpec(eq("o1"), eq(false), any(ValueFactory.class));
        doReturn(new BasicPropertySpec("o2", false, new StringFactory()))
                .when(propertySpecService).basicPropertySpec(eq("o2"), eq(false), any(ValueFactory.class));
        doReturn(new BasicPropertySpec("o3", false, new StringFactory()))
                .when(propertySpecService).basicPropertySpec(eq("o3"), eq(false), any(ValueFactory.class));
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        final List<DeviceProtocolDialect> deviceProtocolDialects = meterProtocolAdapter.getDeviceProtocolDialects();

        // asserts
        assertThat(deviceProtocolDialects).hasSize(1);
        final DeviceProtocolDialect deviceProtocolDialect = deviceProtocolDialects.get(0);
        assertThat(getOptionalPropertiesFromSet(deviceProtocolDialect.getPropertySpecs())).isEmpty();
        assertThat(getRequiredPropertiesFromSet(deviceProtocolDialect.getPropertySpecs())).isEmpty();
    }

    @Test
    public void versionTest() {
        final String version = "ProtocolVersion";
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        when(meterProtocol.getProtocolVersion()).thenReturn(version);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(meterProtocolAdapter.getVersion()).isEqualTo(version);
    }

    @Test(expected = LegacyProtocolException.class)
    public void invalidPropertyAdditionTest() throws InvalidPropertyException, MissingPropertyException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        TypedProperties properties = TypedProperties.empty();
        doThrow(new InvalidPropertyException("Invalid property received")).when(meterProtocol).setProperties(Matchers.<Properties>any());
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
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
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
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
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(meterProtocolAdapter.getSerialNumber()).isEqualTo(meterSerialNumber);
    }

    @Test
    public void cachingProtocolTests() throws SQLException {
        final Object cacheObject = new BigDecimal("1256.6987");
        final int deviceId = 123;
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);

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
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);

        // call the logOn business method
        meterProtocolAdapter.logOn();

        // verify that the adapter properly called the connect() of the meterProtocol
        verify(meterProtocol).connect();
    }

    @Test
    public void logOnWithExceptionTest() throws CommunicationException, IOException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        doThrow(new IOException("Connection failed for a test reason")).when(meterProtocol).connect();
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);

        try {
            // call the logOn business method
            meterProtocolAdapter.logOn();
        } catch (CommunicationException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.PROTOCOL_CONNECT)) {
                throw e;
            }
        }
    }

    @Test
    public void logOffWithoutViolationsTest() throws IOException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);

        // call the business method
        meterProtocolAdapter.logOff();

        // verify that the adapter properly called the connect() of the meterProtocol
        verify(meterProtocol).disconnect();
    }

    @Test
    public void logOffWithExceptionTest() throws IOException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        doThrow(new IOException("Disconnect failed for a test reason")).when(meterProtocol).disconnect();
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);

        try {
            // call the business method
            meterProtocolAdapter.logOff();
        } catch (CommunicationException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.PROTOCOL_DISCONNECT)) {
                throw e;
            }
        }
    }

    @Test
    public void enableHHUSignOnWithoutExceptionsTest() throws ConnectionException {
        HhuEnabledMeterProtocol meterProtocol = mock(HhuEnabledMeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);

        // call the business method
        meterProtocolAdapter.enableHHUSignOn(any(SerialCommunicationChannel.class));

        // verify that the adapter properly called the method of the meterProtocol
        verify(meterProtocol).enableHHUSignOn(any(SerialCommunicationChannel.class));
    }

    @Test
    public void enableHHUSignOnWithoutExceptionsWithDataReadOut() throws ConnectionException {
        HhuEnabledMeterProtocol meterProtocol = mock(HhuEnabledMeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);

        // call the business method
        meterProtocolAdapter.enableHHUSignOn(any(SerialCommunicationChannel.class), anyBoolean());

        // verify that the adapter properly called the method of the meterProtocol
        verify(meterProtocol).enableHHUSignOn(any(SerialCommunicationChannel.class), anyBoolean());
    }

    @Test
    public void getHHUDataReadoutTest() {
        HhuEnabledMeterProtocol meterProtocol = mock(HhuEnabledMeterProtocol.class, withSettings().extraInterfaces(DeviceSecuritySupport.class, DeviceMessageSupport.class));
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);

        // call the business method
        meterProtocolAdapter.getHHUDataReadout();

        // verify that the adapter properly called the method of the meterProtocol
        verify(meterProtocol).getHHUDataReadout();
    }

    @Test
    public void getHHUDataReadoutEmptyByteArray() {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapterImpl meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);

        // call the business method
        byte[] hhuDataReadout = meterProtocolAdapter.getHHUDataReadout();

        // verify that we received an empty byteArray
        assertThat(hhuDataReadout).isNotNull();
        assertThat(hhuDataReadout.length).isZero();
    }

    @Test
    public void getCapabilitiesTest() throws ClassNotFoundException {
        this.inMemoryPersistence.run(dataModel -> {
            DeviceCapabilityAdapterMappingImpl deviceCapabilityMapping = new DeviceCapabilityAdapterMappingImpl(MockDeviceProtocol.class.getCanonicalName(), 6);
            dataModel.persist(deviceCapabilityMapping);
        });
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        MeterProtocolAdapterImpl meterProtocolAdapter = spy(newMeterProtocolAdapter(meterProtocol));
        when(meterProtocolAdapter.getProtocolClass()).thenReturn(MockDeviceProtocol.class);

        // assert that the adapter provides all capabilities
        assertThat(meterProtocolAdapter.getDeviceProtocolCapabilities()).containsOnly(
                DeviceProtocolCapabilities.PROTOCOL_SESSION, DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Test
    public void testGetSecurityPropertiesWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        MeterProtocol adaptedProtocol = new SimpleTestMeterProtocol();
        MeterProtocolAdapterImpl adapter = new TestMeterProtocolAdapter(adaptedProtocol, this.inMemoryPersistence.getPropertySpecService(), this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        List<PropertySpec> securityProperties = adapter.getSecurityPropertySpecs();

        // Asserts
        assertThat(securityProperties).isEqualTo(new SimpleTestDeviceSecuritySupport(inMemoryPersistence.getPropertySpecService()).getSecurityPropertySpecs());
    }

    @Test
    public void testGetSecurityPropertiesWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        MeterProtocolAdapterImpl adapter = newMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getSecurityPropertySpecs();

        // Asserts
        verify(adaptedProtocol).getSecurityPropertySpecs();
    }

    @Test
    public void testGetSecurityPropertySpecWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        MeterProtocol adaptedProtocol = new SimpleTestMeterProtocol();
        MeterProtocolAdapterImpl adapter = new TestMeterProtocolAdapter(adaptedProtocol, this.inMemoryPersistence.getPropertySpecService(), this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        Optional<PropertySpec> whatEverPropertySpec = adapter.getSecurityPropertySpec(PROPERTY_SPEC_NAME);
        Optional<PropertySpec> firstPropertySpec = adapter.getSecurityPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName());

        // Asserts
        assertThat(whatEverPropertySpec).isEmpty();
        assertThat(firstPropertySpec).isPresent();
        assertThat(firstPropertySpec.get()).isEqualTo(new PropertySpecServiceImpl().basicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName(), false, new StringFactory()));
    }

    @Test
    public void testGetSecurityPropertySpecWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        MeterProtocolAdapterImpl adapter = newMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getSecurityPropertySpec(PROPERTY_SPEC_NAME);

        // Asserts
        verify(adaptedProtocol).getSecurityPropertySpecs();
    }

    @Test
    public void testGetAuthenticationAccessLevelsWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        MeterProtocol adaptedProtocol = new SimpleTestMeterProtocol();
        MeterProtocolAdapterImpl adapter = new TestMeterProtocolAdapter(adaptedProtocol, this.inMemoryPersistence.getPropertySpecService(), this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = adapter.getAuthenticationAccessLevels();

        // Asserts
        assertThat(authenticationAccessLevels).hasSize(1);
        assertThat(authenticationAccessLevels.get(0).getId()).isEqualTo(SimpleTestDeviceSecuritySupport.AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID);
    }

    @Test
    public void testGetAuthenticationAccessLevelsWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        MeterProtocolAdapterImpl adapter = newMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getAuthenticationAccessLevels();

        // Asserts
        verify(adaptedProtocol).getAuthenticationAccessLevels();
    }

    @Test
    public void testGetEncryptionAccessLevelsWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        MeterProtocol adaptedProtocol = new SimpleTestMeterProtocol();
        MeterProtocolAdapterImpl adapter = new TestMeterProtocolAdapter(adaptedProtocol, this.inMemoryPersistence.getPropertySpecService(), this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        List<EncryptionDeviceAccessLevel> encryptionAccessLevels = adapter.getEncryptionAccessLevels();

        // Asserts
        assertThat(encryptionAccessLevels).hasSize(1);
        assertThat(encryptionAccessLevels.get(0).getId()).isEqualTo(SimpleTestDeviceSecuritySupport.ENCRYPTION_DEVICE_ACCESS_LEVEL_ID);
    }

    @Test
    public void testGetEncryptionAccessLevelsWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        MeterProtocolAdapterImpl adapter = newMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getEncryptionAccessLevels();

        // Asserts
        verify(adaptedProtocol).getEncryptionAccessLevels();
    }

    @Test
    public void getFirmwareVersionTest() throws IOException {
        String myTestFirmwareVersion = "jslmjksdfjjL1321";
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        when(meterProtocol.getFirmwareVersion()).thenReturn(myTestFirmwareVersion);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(offlineDevice.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        MeterProtocolAdapter meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());

        CollectedFirmwareVersion collectedFirmwareVersion = mock(CollectedFirmwareVersion.class);
        when(collectedDataFactory.createFirmwareVersionsCollectedData(deviceIdentifier)).thenReturn(collectedFirmwareVersion);
        CollectedFirmwareVersion firmwareVersions = meterProtocolAdapter.getFirmwareVersions();

        verify(collectedFirmwareVersion).setActiveMeterFirmwareVersion(myTestFirmwareVersion);
    }

    @Test
    public void getFirmwareVersionWithExceptionest() throws IOException {
        MeterProtocol meterProtocol = getMockedMeterProtocol();
        IOException expectedException = createGetFirmwareVersionIOException();
        when(meterProtocol.getFirmwareVersion()).thenThrow(expectedException);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(offlineDevice.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        MeterProtocolAdapter meterProtocolAdapter = newMeterProtocolAdapter(meterProtocol);
        meterProtocolAdapter.init(offlineDevice, getMockedComChannel());

        CollectedFirmwareVersion collectedFirmwareVersion = mock(CollectedFirmwareVersion.class);
        when(collectedDataFactory.createFirmwareVersionsCollectedData(deviceIdentifier)).thenReturn(collectedFirmwareVersion);
        try {
            CollectedFirmwareVersion firmwareVersions = meterProtocolAdapter.getFirmwareVersions();
            fail("Shoud not get here!");
        } catch (LegacyProtocolException e) {
            assertThat(e.getCause()).isEqualTo(expectedException);
        }
    }

    private IOException createGetFirmwareVersionIOException() {
        return new IOException("MyExpectedIoException");
    }

    protected MeterProtocolAdapterImpl newMeterProtocolAdapter(MeterProtocol meterProtocol) {
        return new MeterProtocolAdapterImpl(meterProtocol, this.inMemoryPersistence.getPropertySpecService(), this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.capabilityAdapterMappingFactory, messageAdapterMappingFactory, this.protocolPluggableService.getDataModel(), this.inMemoryPersistence.getIssueService(), collectedDataFactory, meteringService, thesaurus);
    }

    private interface MeterProtocolWithDeviceSecuritySupport extends MeterProtocol, DeviceSecuritySupport {
    }

    private class TestMeterProtocolAdapter extends MeterProtocolAdapterImpl {

        private TestMeterProtocolAdapter(MeterProtocol meterProtocol, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService1, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory) {
            super(meterProtocol, propertySpecService, protocolPluggableService1, securitySupportAdapterMappingFactory, capabilityAdapterMappingFactory, messageAdapterMappingFactory, protocolPluggableService.getDataModel(), inMemoryPersistence.getIssueService(), collectedDataFactory, meteringService, thesaurus);
        }

        @Override
        protected void initializeAdapters() {
            setMeterProtocolSecuritySupportAdapter(
                    new MeterProtocolSecuritySupportAdapter(
                            getMeterProtocol(),
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