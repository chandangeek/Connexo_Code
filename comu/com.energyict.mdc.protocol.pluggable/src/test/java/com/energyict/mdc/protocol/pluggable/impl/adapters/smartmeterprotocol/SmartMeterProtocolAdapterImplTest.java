/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecBuilderWizard;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.PropertySpecMockSupport;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceCapabilityAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecurityProperties;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock.HhuEnabledSmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for the {@link SmartMeterProtocolAdapterImpl}.
 *
 * @author gna
 * @since 5/04/12 - 13:20
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartMeterProtocolAdapterImplTest {

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
    @Mock
    private Thesaurus thesaurus;

    private InMemoryPersistence inMemoryPersistence;
    private ProtocolPluggableServiceImpl protocolPluggableService;
    private PropertySpecMockSupport propertySpecMockSupport;

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
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        protocolPluggableService.addCollectedDataFactory(this.collectedDataFactory);
        DeviceProtocolSecurityService deviceProtocolSecurityService = this.inMemoryPersistence.getDeviceProtocolSecurityService();
        PropertySpecService propertySpecService = inMemoryPersistence.getPropertySpecService();
        this.propertySpecMockSupport = new PropertySpecMockSupport();
        propertySpecMockSupport.mockStringPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName(), propertySpecService);
        propertySpecMockSupport.mockStringPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.SECOND.javaName(), propertySpecService);
        propertySpecMockSupport.mockStringPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.THIRD.javaName(), propertySpecService);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("whatever");
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        doReturn(thesaurusOptions).when(nlsOptions).named(any(String.class), any(TranslationKey.class));
        doReturn(thesaurusOptions).when(nlsOptions).named(any(TranslationKey.class));
        when(propertySpecService.timezoneSpec()).thenReturn(nlsOptions);
        SimpleTestDeviceSecuritySupport securitySupport = new SimpleTestDeviceSecuritySupport(propertySpecService);
        when(deviceProtocolSecurityService.createDeviceProtocolSecurityFor(SimpleTestDeviceSecuritySupport.class.getName()))
            .thenReturn(securitySupport);

        when(this.securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol
                (SimpleTestSmartMeterProtocol.class.getName())).thenReturn(SimpleTestDeviceSecuritySupport.class.getName());

        this.mockPropertySpecs();
    }

    private void mockPropertySpecs() {
        this.propertySpecMockSupport.mockStringPropertySpec(MeterProtocol.NODEID, inMemoryPersistence.getPropertySpecService());
        this.propertySpecMockSupport.mockStringPropertySpec(MeterProtocol.ADDRESS, inMemoryPersistence.getPropertySpecService());
        this.propertySpecMockSupport.mockStringPropertySpec(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName(), inMemoryPersistence.getPropertySpecService());
        this.propertySpecMockSupport.mockStringPropertySpec(DeviceProtocolProperty.DEVICE_TIME_ZONE.javaFieldName(), inMemoryPersistence.getPropertySpecService());
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
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(smartMeterProtocolAdapter.getSmartMeterProtocolClockAdapter()).isNotNull();
        assertThat(smartMeterProtocolAdapter.getSmartMeterProtocolLoadProfileAdapter()).isNotNull();
        assertThat(smartMeterProtocolAdapter.getDeviceProtocolTopologyAdapter()).isNotNull();
        assertThat(smartMeterProtocolAdapter.getSmartMeterProtocolLogBookAdapter()).isNotNull();
        assertThat(smartMeterProtocolAdapter.getSmartMeterProtocolRegisterAdapter()).isNotNull();
    }

    @Test(expected = LegacyProtocolException.class)
    public void terminateExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        doThrow(new IOException("Could not terminate/release the protocol")).when(smartMeterProtocol).release();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        smartMeterProtocolAdapter.terminate();
    }

    @Test
    public void getSerialNumberTest() throws IOException {
        final String meterSerialNumber = "MeterSerialNumber";
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getMeterSerialNumber()).thenReturn(meterSerialNumber);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(smartMeterProtocolAdapter.getSerialNumber()).isEqualTo(meterSerialNumber);
    }

    @Test(expected = LegacyProtocolException.class)
    public void getSerialNumberExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getMeterSerialNumber()).thenThrow(new IOException("Could not fetch the serialnumber"));
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        smartMeterProtocolAdapter.getSerialNumber();
    }

    @Test
    public void getRequiredKeysCorrectTest() {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        List<PropertySpec> requiredKeys = new ArrayList<>();
        requiredKeys.add(this.newStringPropertySpec("r1"));
        requiredKeys.add(this.newStringPropertySpec("r2"));
        requiredKeys.add(this.newStringPropertySpec("r3"));
        when(smartMeterProtocol.getRequiredProperties()).thenReturn(requiredKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(getRequiredPropertiesFromSet(smartMeterProtocolAdapter.getPropertySpecs())).isEmpty(); // the optional properties are replaced by the hardcoded legacy values
        assertThat(getOptionalPropertiesFromSet(smartMeterProtocolAdapter.getPropertySpecs())).isNotEmpty(); // the optional properties are replaced by the hardcoded legacy values
    }

    private PropertySpec newStringPropertySpec(String name) {
        return new PropertySpecServiceImpl().stringSpec().named(name, name).describedAs(name).finish();
    }

    @Test
    public void getOptionalKeysCorrectTest() {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        List<PropertySpec> optionalKeys = new ArrayList<>();
        optionalKeys.add(this.newStringPropertySpec("o1"));
        optionalKeys.add(this.newStringPropertySpec("o2"));
        optionalKeys.add(this.newStringPropertySpec("o3"));
        when(smartMeterProtocol.getOptionalProperties()).thenReturn(optionalKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(getRequiredPropertiesFromSet(smartMeterProtocolAdapter.getPropertySpecs())).isEmpty();
        assertThat(getOptionalPropertiesFromSet(smartMeterProtocolAdapter.getPropertySpecs())).isNotEmpty();
        assertThat(smartMeterProtocolAdapter.getPropertySpecs()).hasSize(7);
    }

    @Test
    public void getDeviceProtocolDialect() {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(((DeviceSecuritySupport) smartMeterProtocol).getCustomPropertySet()).thenReturn(Optional.empty());
        List<PropertySpec> optionalKeys = new ArrayList<>();
        optionalKeys.add(this.newStringPropertySpec("o1"));
        optionalKeys.add(this.newStringPropertySpec("o2"));
        optionalKeys.add(this.newStringPropertySpec("o3"));
        when(smartMeterProtocol.getOptionalProperties()).thenReturn(optionalKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        final List<DeviceProtocolDialect> deviceProtocolDialects = smartMeterProtocolAdapter.getDeviceProtocolDialects();

        // asserts
        assertThat(deviceProtocolDialects).hasSize(1);
        final DeviceProtocolDialect deviceProtocolDialect = deviceProtocolDialects.get(0);
        assertThat(getOptionalPropertiesFromSet(deviceProtocolDialect.getPropertySpecs())).isEmpty();
    }

    @Test
    public void getVersionTest() {
        final String version = "SmartMeterProtocolVersion";
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getVersion()).thenReturn(version);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertThat(smartMeterProtocolAdapter.getVersion()).isEqualTo(version);
    }

    @Test
    public void cachingProtocolTests() throws SQLException {
        final Object cacheObject = new BigDecimal("1256.6987");
        final int deviceId = 123;
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

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
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the logOn business method
        smartMeterProtocolAdapter.logOn();

        // verify that the adapter properly called the logOn() of the smartMeterProtocol
        verify(smartMeterProtocol).connect();
    }

    @Test
    public void logOnWithExceptionTest() throws IOException, CommunicationException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        doThrow(new IOException("LogOn failed for a test reason")).when(smartMeterProtocol).connect();
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

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
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the logOn business method
        smartMeterProtocolAdapter.logOff();

        // verify that the adapter properly called the logOn() of the smartMeterProtocol
        verify(smartMeterProtocol).disconnect();
    }

    @Test
    public void logOffWithExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        doThrow(new IOException("LogOff failed for a test reason")).when(smartMeterProtocol).disconnect();
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

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
        SmartMeterProtocolAdapterImpl meterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the business method
        meterProtocolAdapter.enableHHUSignOn(any(SerialCommunicationChannel.class));

        // verify that the adapter properly called the method of the smartMeterProtocol
        verify(smartMeterProtocol).enableHHUSignOn(any(SerialCommunicationChannel.class));
    }

    @Test
    public void enableHHUSignOnWithoutExceptionsWithDataReadOut() throws ConnectionException {
        HhuEnabledSmartMeterProtocol smartMeterProtocol = getMockedHHUEnabledSmartMeterProtocol();
        SmartMeterProtocolAdapterImpl meterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the business method
        meterProtocolAdapter.enableHHUSignOn(any(SerialCommunicationChannel.class), anyBoolean());

        // verify that the adapter properly called the method of the smartMeterProtocol
        verify(smartMeterProtocol).enableHHUSignOn(any(SerialCommunicationChannel.class), anyBoolean());
    }

    @Test
    public void getHHUDataReadoutTest() {
        HhuEnabledSmartMeterProtocol smartMeterProtocol = getMockedHHUEnabledSmartMeterProtocol();
        SmartMeterProtocolAdapterImpl meterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

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
        SmartMeterProtocolAdapterImpl meterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);

        // call the business method
        byte[] hhuDataReadout = meterProtocolAdapter.getHHUDataReadout();

        // verify that we received an empty byteArray
        assertThat(hhuDataReadout).isNotNull();
        assertThat(hhuDataReadout.length).isEqualTo(0);
    }

    @Test
    public void getCapabilitiesTest() throws ClassNotFoundException {
        SmartMeterProtocol meterProtocol = getMockedSmartMeterProtocol();
        SmartMeterProtocolAdapterImpl meterProtocolAdapter = spy(newSmartMeterProtocolAdapter(meterProtocol));
        when(meterProtocolAdapter.getProtocolClass()).thenReturn(MockDeviceProtocol.class);

        // assert that the adapter provides all capabilities
        assertThat(meterProtocolAdapter.getDeviceProtocolCapabilities()).containsOnly(
                DeviceProtocolCapabilities.PROTOCOL_SESSION, DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Test
    public void testGetSecurityPropertiesWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapterImpl adapter =
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
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(adaptedProtocol.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));
        SmartMeterProtocolAdapterImpl adapter = newSmartMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getSecurityPropertySpecs();

        // Asserts
        verify(adaptedProtocol).getCustomPropertySet();
        verify(customPropertySet).getPropertySpecs();
    }

    @Test
    public void testGetSecurityPropertySpecWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapterImpl adapter =
                new TestSmartMeterProtocolAdapter(
                        adaptedProtocol,
                        this.inMemoryPersistence.getPropertySpecService(),
                        this.protocolPluggableService,
                        this.securitySupportAdapterMappingFactory,
                        this.capabilityAdapterMappingFactory,
                        this.messageAdapterMappingFactory,
                        this.collectedDataFactory);

        // Business method
        Optional<PropertySpec> whatEverPropertySpec = adapter.getSecurityPropertySpec(PROPERTY_SPEC_NAME);
        Optional<PropertySpec> firstPropertySpec = adapter.getSecurityPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName());

        // Asserts
        assertThat(whatEverPropertySpec).isEmpty();
        assertThat(firstPropertySpec).isPresent();
        PropertySpec propertySpec = firstPropertySpec.get();
        assertThat(propertySpec.getName()).isEqualTo(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName());
        assertThat(propertySpec.isRequired()).isFalse();
    }

    @Test
    public void testGetSecurityPropertySpecWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        SmartMeterProtocolAdapterImpl adapter = newSmartMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getSecurityPropertySpec(PROPERTY_SPEC_NAME);

        // Asserts
        verify(adaptedProtocol).getSecurityPropertySpec(PROPERTY_SPEC_NAME);
    }

    @Test
    public void testGetAuthenticationAccessLevelsWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapterImpl adapter =
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
        SmartMeterProtocolAdapterImpl adapter = newSmartMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getAuthenticationAccessLevels();

        // Asserts
        verify(adaptedProtocol).getAuthenticationAccessLevels();
    }

    @Test
    public void testGetEncryptionAccessLevelsWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapterImpl adapter =
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
        SmartMeterProtocolAdapterImpl adapter = newSmartMeterProtocolAdapter(adaptedProtocol);

        // Business method
        adapter.getEncryptionAccessLevels();

        // Asserts
        verify(adaptedProtocol).getEncryptionAccessLevels();
    }

    @Test
    public void getFirmwareVersionTest() throws IOException {
        String myTestFirmwareVersion = "jslmjksdfjjL1321";
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getFirmwareVersion()).thenReturn(myTestFirmwareVersion);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(offlineDevice.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());

        CollectedFirmwareVersion collectedFirmwareVersion = mock(CollectedFirmwareVersion.class);
        when(collectedDataFactory.createFirmwareVersionsCollectedData(deviceIdentifier)).thenReturn(collectedFirmwareVersion);
        CollectedFirmwareVersion firmwareVersions = smartMeterProtocolAdapter.getFirmwareVersions();

        verify(collectedFirmwareVersion).setActiveMeterFirmwareVersion(myTestFirmwareVersion);
    }

    @Test
    public void getFirmwareVersionLegacyExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        IOException expectedException = createLegacyIOException();
        when(smartMeterProtocol.getFirmwareVersion()).thenThrow(expectedException);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(offlineDevice.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());

        CollectedFirmwareVersion collectedFirmwareVersion = mock(CollectedFirmwareVersion.class);
        when(collectedDataFactory.createFirmwareVersionsCollectedData(deviceIdentifier)).thenReturn(collectedFirmwareVersion);
        try {
            smartMeterProtocolAdapter.getFirmwareVersions();
            fail("Shoud not get here!");
        } catch (LegacyProtocolException e) {
            assertThat(e.getCause()).isEqualTo(expectedException);
        }
    }

    private IOException createLegacyIOException() {
        return new IOException("MyExpectedIoException");
    }

    @Test
    public void getBreakerStatusTest() throws IOException {
        BreakerStatus myBreakerStatus = BreakerStatus.ARMED;
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getBreakerStatus()).thenReturn(Optional.of(myBreakerStatus));
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(offlineDevice.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        CollectedBreakerStatus collectedBreakerStatus = mock(CollectedBreakerStatus.class);
        when(collectedDataFactory.createBreakerStatusCollectedData(deviceIdentifier)).thenReturn(collectedBreakerStatus);

        // Business method
        CollectedBreakerStatus breakerStatus = smartMeterProtocolAdapter.getBreakerStatus();

        // Asserts
        verify(collectedBreakerStatus).setBreakerStatus(myBreakerStatus);
    }

    @Test
    public void getBreakerStatusLegacyExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        IOException expectedException = createLegacyIOException();
        when(smartMeterProtocol.getBreakerStatus()).thenThrow(expectedException);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(offlineDevice.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        SmartMeterProtocolAdapterImpl smartMeterProtocolAdapter = newSmartMeterProtocolAdapter(smartMeterProtocol);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        CollectedBreakerStatus collectedBreakerStatus = mock(CollectedBreakerStatus.class);
        when(collectedDataFactory.createBreakerStatusCollectedData(deviceIdentifier)).thenReturn(collectedBreakerStatus);

        // Business method
        try {
            smartMeterProtocolAdapter.getBreakerStatus();
            fail("Shoud not get here!");
        } catch (LegacyProtocolException e) {
            assertThat(e.getCause()).isEqualTo(expectedException);
        }
    }

    protected SmartMeterProtocolAdapterImpl newSmartMeterProtocolAdapter(SmartMeterProtocol smartMeterProtocol) {
        DataModel dataModel = this.protocolPluggableService.getDataModel();
        return new SmartMeterProtocolAdapterImpl(
                smartMeterProtocol,
                this.inMemoryPersistence.getPropertySpecService(),
                this.protocolPluggableService,
                this.securitySupportAdapterMappingFactory,
                new CapabilityAdapterMappingFactoryImpl(dataModel),
                messageAdapterMappingFactory,
                dataModel,
                this.inMemoryPersistence.getIssueService(),
                collectedDataFactory,
                meteringService, thesaurus);
    }

    private interface MeterProtocolWithDeviceSecuritySupport extends SmartMeterProtocol, DeviceSecuritySupport {
    }

    private class TestSmartMeterProtocolAdapter extends SmartMeterProtocolAdapterImpl {

        private TestSmartMeterProtocolAdapter(SmartMeterProtocol meterProtocol, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService1, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, CapabilityAdapterMappingFactory capabilityAdapterMappingFactory, MessageAdapterMappingFactory messageAdapterMappingFactory, CollectedDataFactory collectedDataFactory) {
            super(meterProtocol, propertySpecService, protocolPluggableService1, securitySupportAdapterMappingFactory, capabilityAdapterMappingFactory, messageAdapterMappingFactory, protocolPluggableService.getDataModel(), inMemoryPersistence.getIssueService(), collectedDataFactory, meteringService, thesaurus);
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