package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.events.impl.Bus;
import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.mdc.dynamic.relation.impl.ServiceLocator;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceCapabilityAdapterMappingFactoryProvider;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.LegacyPropertySpecSupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock.HhuEnabledSmartMeterProtocol;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import org.fest.assertions.api.Assertions;
import org.fest.assertions.core.Condition;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link SmartMeterProtocolAdapter}
 *
 * @author gna
 * @since 5/04/12 - 13:20
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartMeterProtocolAdapterTest {

    private static final String PROPERTY_SPEC_NAME = "whatever";

    private static final String AS220_PROTOCOL = "com.energyict.protocolimpl.dlms.as220.AS220";

    @Mock
    private static UserEnvironment userEnvironment = mock(UserEnvironment.class);
    @Mock
    private Environment environment;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;
    @Mock
    private CapabilityAdapterMappingFactoryImpl capabilityAdapterMappingFactory;

    private IssueServiceImpl issueService;

    @BeforeClass
    public static void initializeUserEnvironment() {
        UserEnvironment.setDefault(userEnvironment);
        when(userEnvironment.getErrorMsg(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }
        });
    }

    @AfterClass
    public static void cleanupUserEnvironment() {
        UserEnvironment.setDefault(null);
    }

    @Before
    public void initializeEnvironment() {
        Environment.DEFAULT.set(this.environment);
        when(this.environment.getErrorMsg(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });
        when(deviceProtocolSecurityService.createDeviceProtocolSecurityFor(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                String javaClassName = (String) invocationOnMock.getArguments()[0];
                try {
                    return Class.forName(javaClassName).newInstance();
                } catch (ClassNotFoundException e) {
                    throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceSecuritySupportClass(e, javaClassName);
                }
            }
        });
        issueService = new IssueServiceImpl();
        issueService.setClock(new DefaultClock());
        com.energyict.mdc.issues.Bus.setIssueService(issueService);
        when(serviceLocator.getDeviceProtocolSecurityService()).thenReturn(deviceProtocolSecurityService);
        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void cleanupEnvironment() {
        Environment.DEFAULT.set(null);
        Bus.clearServiceLocator(serviceLocator);
        com.energyict.mdc.issues.Bus.clearIssueService(issueService);
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

    @Before
    public void before() {
        when(this.securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol
                ("com.energyict.comserver.adapters.smartmeterprotocol.SimpleTestSmartMeterProtocol")).thenReturn("com.energyict.comserver.adapters.common.SimpleTestDeviceSecuritySupport");

        final DeviceCapabilityAdapterMappingFactoryProvider deviceCapabilityAdapterMappingFactoryProvider = mock(DeviceCapabilityAdapterMappingFactoryProvider.class);
        when(deviceCapabilityAdapterMappingFactoryProvider.getCapabilityAdapterMappingFactory()).thenReturn(capabilityAdapterMappingFactory);
        when(deviceCapabilityAdapterMappingFactoryProvider.getCapabilityAdapterMappingFactory().getCapabilitiesMappingForDeviceProtocol(TestDeviceProtocol.class.getCanonicalName())).thenReturn(6);  //6 = master and session capability
        DeviceCapabilityAdapterMappingFactoryProvider.INSTANCE.set(deviceCapabilityAdapterMappingFactoryProvider);
    }

    @Test
    public void smartMeterProtocolAdaptersNotNull() {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);
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
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        smartMeterProtocolAdapter.terminate();
    }

    @Test
    public void getSerialNumberTest() throws IOException {
        final String meterSerialNumber = "MeterSerialNumber";
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getMeterSerialNumber()).thenReturn(meterSerialNumber);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertEquals(meterSerialNumber, smartMeterProtocolAdapter.getSerialNumber());
    }

    @Test(expected = LegacyProtocolException.class)
    public void getSerialNumberExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getMeterSerialNumber()).thenThrow(new IOException("Could not fetch the serialnumber"));
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);
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
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);
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
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);
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


    @Test
    public void getDeviceProtocolDialect() {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        List<com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec> optionalKeys = new ArrayList<>();
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o1"));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o2"));
        optionalKeys.add(PropertySpecFactory.stringPropertySpec("o3"));
        when(smartMeterProtocol.getOptionalProperties()).thenReturn(optionalKeys);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        final List<DeviceProtocolDialect> deviceProtocolDialects = smartMeterProtocolAdapter.getDeviceProtocolDialects();

        // asserts
        assertThat(deviceProtocolDialects).hasSize(1);
        final DeviceProtocolDialect deviceProtocolDialect = deviceProtocolDialects.get(0);
        assertThat(getOptionalPropertiesFromSet(deviceProtocolDialect.getPropertySpecs())).contains(LegacyPropertySpecSupport.toPropertySpecs(optionalKeys, false).toArray(new PropertySpec[optionalKeys.size()]));
        assertThat(getRequiredPropertiesFromSet(deviceProtocolDialect.getPropertySpecs())).isEmpty();
    }

    @Test
    public void getVersionTest() {
        final String version = "SmartMeterProtocolVersion";
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        when(smartMeterProtocol.getVersion()).thenReturn(version);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);
        smartMeterProtocolAdapter.init(offlineDevice, getMockedComChannel());
        assertEquals(version, smartMeterProtocolAdapter.getVersion());
    }

    @Test
    public void cachingProtocolTests() throws BusinessException, SQLException {
        final Object cacheObject = new BigDecimal("1256.6987");
        final int deviceId = 123;
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

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
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // call the logOn business method
        smartMeterProtocolAdapter.logOn();

        // verify that the adapter properly called the logOn() of the smartMeterProtocol
        verify(smartMeterProtocol).connect();
    }

    @Test
    public void logOnWithExceptionTest() throws IOException, CommunicationException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        doThrow(new IOException("LogOn failed for a test reason")).when(smartMeterProtocol).connect();
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        try {
            // call the logOn business method
            smartMeterProtocolAdapter.logOn();
        } catch (CommunicationException e) {
            if (!e.getMessage().equals(Environment.DEFAULT.get().getErrorMsg("CSC-COM-113"))) {
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
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // call the logOn business method
        smartMeterProtocolAdapter.logOff();

        // verify that the adapter properly called the logOn() of the smartMeterProtocol
        verify(smartMeterProtocol).disconnect();
    }

    @Test
    public void logOffWithExceptionTest() throws IOException {
        SmartMeterProtocol smartMeterProtocol = getMockedSmartMeterProtocol();
        doThrow(new IOException("LogOff failed for a test reason")).when(smartMeterProtocol).disconnect();
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        try {
            // call the logOn business method
            smartMeterProtocolAdapter.logOff();
        } catch (CommunicationException e) {
            if (!e.getMessage().equals(Environment.DEFAULT.get().getErrorMsg("CSC-COM-114"))) {
                throw e;
            }
        }
    }


    @Test
    public void enableHHUSignOnWithoutExceptionsTest() throws ConnectionException {
        HhuEnabledSmartMeterProtocol smartMeterProtocol = getMockedHHUEnabledSmartMeterProtocol();
        SmartMeterProtocolAdapter meterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // call the business method
        meterProtocolAdapter.enableHHUSignOn(any(SerialCommunicationChannel.class));

        // verify that the adapter properly called the method of the smartMeterProtocol
        verify(smartMeterProtocol).enableHHUSignOn(any(SerialCommunicationChannel.class));
    }

    @Test
    public void enableHHUSignOnWithoutExceptionsWithDataReadOut() throws ConnectionException {
        HhuEnabledSmartMeterProtocol smartMeterProtocol = getMockedHHUEnabledSmartMeterProtocol();
        SmartMeterProtocolAdapter meterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // call the business method
        meterProtocolAdapter.enableHHUSignOn(any(SerialCommunicationChannel.class), anyBoolean());

        // verify that the adapter properly called the method of the smartMeterProtocol
        verify(smartMeterProtocol).enableHHUSignOn(any(SerialCommunicationChannel.class), anyBoolean());
    }

    @Test
    public void getHHUDataReadoutTest() {
        HhuEnabledSmartMeterProtocol smartMeterProtocol = getMockedHHUEnabledSmartMeterProtocol();
        SmartMeterProtocolAdapter meterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

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
        SmartMeterProtocolAdapter meterProtocolAdapter = new SmartMeterProtocolAdapter(smartMeterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // call the business method
        byte[] hhuDataReadout = meterProtocolAdapter.getHHUDataReadout();

        // verify that we received an empty byteArray
        assertNotNull(hhuDataReadout);
        assertEquals(0, hhuDataReadout.length);
    }

    @Test
    public void getCapabilitiesTest() throws ClassNotFoundException {
        SmartMeterProtocol meterProtocol = getMockedSmartMeterProtocol();
        SmartMeterProtocolAdapter meterProtocolAdapter = spy(new SmartMeterProtocolAdapter(meterProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory));
        when(meterProtocolAdapter.getProtocolClass()).thenReturn(TestDeviceProtocol.class);

        // assert that the adapter provides all capabilities
        assertThat(meterProtocolAdapter.getDeviceProtocolCapabilities()).containsOnly(
                DeviceProtocolCapabilities.PROTOCOL_SESSION, DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Test
    public void testGetSecurityPropertiesWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapter adapter = new TestSmartMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        List<PropertySpec> securityProperties = adapter.getSecurityProperties();

        // Asserts
        assertThat(securityProperties).isEqualTo(new SimpleTestDeviceSecuritySupport().getSecurityProperties());
    }

    @Test
    public void testGetSecurityPropertiesWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        SmartMeterProtocolAdapter adapter = new SmartMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        adapter.getSecurityProperties();

        // Asserts
        verify(adaptedProtocol).getSecurityProperties();
    }

    @Test
    public void testGetSecurityPropertySpecWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapter adapter = new TestSmartMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

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
        SmartMeterProtocolAdapter adapter = new SmartMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        adapter.getSecurityPropertySpec(PROPERTY_SPEC_NAME);

        // Asserts
        verify(adaptedProtocol).getSecurityPropertySpec(PROPERTY_SPEC_NAME);
    }

    @Test
    public void testGetAuthenticationAccessLevelsWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapter adapter = new TestSmartMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = adapter.getAuthenticationAccessLevels();

        // Asserts
        assertThat(authenticationAccessLevels).hasSize(1);
        assertThat(authenticationAccessLevels.get(0).getId()).isEqualTo(SimpleTestDeviceSecuritySupport.AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID);
    }

    @Test
    public void testGetAuthenticationAccessLevelsWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        SmartMeterProtocolAdapter adapter = new SmartMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        adapter.getAuthenticationAccessLevels();

        // Asserts
        verify(adaptedProtocol).getAuthenticationAccessLevels();
    }

    @Test
    public void testGetEncryptionAccessLevelsWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapter adapter = new TestSmartMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        List<EncryptionDeviceAccessLevel> encryptionAccessLevels = adapter.getEncryptionAccessLevels();

        // Asserts
        assertThat(encryptionAccessLevels).hasSize(1);
        assertThat(encryptionAccessLevels.get(0).getId()).isEqualTo(SimpleTestDeviceSecuritySupport.ENCRYPTION_DEVICE_ACCESS_LEVEL_ID);
    }

    @Test
    public void testGetEncryptionAccessLevelsWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        SmartMeterProtocolAdapter adapter = new SmartMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        adapter.getEncryptionAccessLevels();

        // Asserts
        verify(adaptedProtocol).getEncryptionAccessLevels();
    }

    @Test
    public void testGetSecurityRelationTypeNameWhenWrappedProtocolDoesNotImplementDeviceSecuritySupport() {
        SmartMeterProtocol adaptedProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolAdapter adapter = new TestSmartMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Asserts
        Assertions.assertThat(SimpleTestDeviceSecuritySupport.DUMMY_RELATION_TYPE_NAME).isEqualTo(adapter.getSecurityRelationTypeName());
    }

    @Test
    public void testGetSecurityRelationTypeNameWhenWrappedProtocolImplementsDeviceSecuritySupport() {
        MeterProtocolWithDeviceSecuritySupport adaptedProtocol = mock(MeterProtocolWithDeviceSecuritySupport.class, withSettings().extraInterfaces(DeviceMessageSupport.class));
        SmartMeterProtocolAdapter adapter = new SmartMeterProtocolAdapter(adaptedProtocol, this.protocolPluggableService, this.securitySupportAdapterMappingFactory);

        // Business method
        adapter.getSecurityRelationTypeName();

        // Asserts
        verify(adaptedProtocol).getSecurityRelationTypeName();
    }

    private interface MeterProtocolWithDeviceSecuritySupport extends SmartMeterProtocol, DeviceSecuritySupport {

    }

    private class TestSmartMeterProtocolAdapter extends SmartMeterProtocolAdapter {

        private TestSmartMeterProtocolAdapter(final SmartMeterProtocol meterProtocol, ProtocolPluggableService protocolPluggableService, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory) {
            super(meterProtocol, protocolPluggableService, securitySupportAdapterMappingFactory);
        }

        @Override
        protected void initializeAdapters() {
            setSmartMeterProtocolSecuritySupportAdapter(
                    new SmartMeterProtocolSecuritySupportAdapter(
                            getSmartMeterProtocol(),
                            mock(PropertiesAdapter.class),
                            this.getSecuritySupportAdapterMappingFactory()));
        }
    }

}