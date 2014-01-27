package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Tests the {@link MeterProtocolSecuritySupportAdapter} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/01/13
 * Time: 9:19
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterProtocolSecuritySupportAdapterTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private SecuritySupportAdapterMappingFactoryImpl securitySupportAdapterMappingFactory;
    @Mock
    private Environment environment;
    @Mock
    private LegacySecurityPropertyConverter legacySecurityPropertyConverter;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;

    @Before
    public void before() {
        when(securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol
                (SimpleTestMeterProtocol.class.getName())).thenReturn(SimpleTestDeviceSecuritySupport.class.getName());
        when(securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol
                (SecondSimpleTestMeterProtocol.class.getName())).thenReturn("com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.NotAKnownDeviceSecuritySupportClass");
        when(securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol
                (ThirdSimpleTestMeterProtocol.class.getName())).thenReturn(ThirdSimpleTestMeterProtocol.class.getName());
        when(this.protocolPluggableService.createDeviceProtocolSecurityFor(SimpleTestDeviceSecuritySupport.class.getName())).thenReturn(new SimpleTestDeviceSecuritySupport());
        doThrow(ProtocolCreationException.class).when(this.protocolPluggableService).
                createDeviceProtocolSecurityFor("com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.NotAKnownDeviceSecuritySupportClass");
        when(this.protocolPluggableService.createDeviceProtocolSecurityFor(ThirdSimpleTestMeterProtocol.class.getName())).thenReturn(new ThirdSimpleTestMeterProtocol());
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
                return Class.forName(javaClassName).newInstance();
            }
        });
    }

    @Test
    public void testKnownMeterProtocol() {
        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        new MeterProtocolSecuritySupportAdapter(
                simpleTestMeterProtocol,
                this.propertySpecService,
                this.protocolPluggableService,
                mock(PropertiesAdapter.class),
                this.securitySupportAdapterMappingFactory);

        // all is safe if no errors occur
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownMeterProtocol() {
        MeterProtocol meterProtocol = mock(MeterProtocol.class);
        try {
            new MeterProtocolSecuritySupportAdapter(
                    meterProtocol,
                    this.propertySpecService,
                    this.protocolPluggableService,
                    mock(PropertiesAdapter.class),
                    this.securitySupportAdapterMappingFactory);
        } catch (DeviceProtocolAdapterCodingExceptions e) {
            if (!e.getMessageId().equals("CSC-DEV-124")) {
                fail("Exception should have indicated that the given meterProtocol is not known in the adapter, but was " + e.getMessage());
            }
            throw e;
        }
        // should have gotten the exception
    }

    @Test
    public void testNotADeviceSecuritySupportClass() {
        MeterProtocol meterProtocol = new ThirdSimpleTestMeterProtocol();
        final MeterProtocolSecuritySupportAdapter spy = spy(new MeterProtocolSecuritySupportAdapter(
                meterProtocol,
                this.propertySpecService,
                this.protocolPluggableService,
                mock(PropertiesAdapter.class),
                this.securitySupportAdapterMappingFactory));

        // asserts
        verify(spy, never()).setLegacySecuritySupport(any(DeviceProtocolSecurityCapabilities.class));
        verify(spy, never()).setLegacySecurityPropertyConverter(any(LegacySecurityPropertyConverter.class));
    }

    @Test
    public void getSecurityPropertiesTest() {
        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        MeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter = new MeterProtocolSecuritySupportAdapter(
                simpleTestMeterProtocol,
                this.propertySpecService,
                this.protocolPluggableService,
                mock(PropertiesAdapter.class),
                this.securitySupportAdapterMappingFactory);

        // business method
        List<PropertySpec> securityProperties = meterProtocolSecuritySupportAdapter.getSecurityProperties();

        // asserts
        assertArrayEquals(securityProperties.toArray(), Arrays.asList(
                SimpleTestDeviceSecuritySupport.firstPropSpec,
                SimpleTestDeviceSecuritySupport.secondPropSpec,
                SimpleTestDeviceSecuritySupport.thirdPropSpec
        ).toArray());
    }

    @Test
    public void getSecurityRelationTypeNameTest() {
        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        MeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter = new MeterProtocolSecuritySupportAdapter(
                simpleTestMeterProtocol,
                this.propertySpecService,
                this.protocolPluggableService,
                mock(PropertiesAdapter.class),
                this.securitySupportAdapterMappingFactory);

        // business method
        String securityRelationTypeName = meterProtocolSecuritySupportAdapter.getSecurityRelationTypeName();

        // asserts
        assertEquals(SimpleTestDeviceSecuritySupport.DUMMY_RELATION_TYPE_NAME, securityRelationTypeName);
    }

    @Test
    public void getSecurityPropertySpecTest() {
        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        MeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter = new MeterProtocolSecuritySupportAdapter(
                simpleTestMeterProtocol,
                this.propertySpecService,
                this.protocolPluggableService,
                mock(PropertiesAdapter.class),
                this.securitySupportAdapterMappingFactory);

        // asserts
        assertEquals(SimpleTestDeviceSecuritySupport.firstPropSpec, meterProtocolSecuritySupportAdapter.getSecurityPropertySpec(SimpleTestDeviceSecuritySupport.FIRST_PROPERTY_NAME));
        assertEquals(SimpleTestDeviceSecuritySupport.secondPropSpec, meterProtocolSecuritySupportAdapter.getSecurityPropertySpec(SimpleTestDeviceSecuritySupport.SECOND_PROPERTY_NAME));
        assertEquals(SimpleTestDeviceSecuritySupport.thirdPropSpec, meterProtocolSecuritySupportAdapter.getSecurityPropertySpec(SimpleTestDeviceSecuritySupport.THIRD_PROPERTY_NAME));
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        MeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter = new MeterProtocolSecuritySupportAdapter(
                simpleTestMeterProtocol,
                this.propertySpecService,
                this.protocolPluggableService,
                mock(PropertiesAdapter.class),
                this.securitySupportAdapterMappingFactory);

        // business method
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = meterProtocolSecuritySupportAdapter.getAuthenticationAccessLevels();

        // asserts
        assertThat(authenticationAccessLevels).hasSize(1);
        assertEquals(SimpleTestDeviceSecuritySupport.AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID, authenticationAccessLevels.get(0).getId());
    }

    @Test
    public void getEncryptionAccessLevelsTest() {
        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        MeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter = new MeterProtocolSecuritySupportAdapter(
                simpleTestMeterProtocol,
                this.propertySpecService,
                this.protocolPluggableService,
                mock(PropertiesAdapter.class),
                this.securitySupportAdapterMappingFactory);

        // business method
        List<EncryptionDeviceAccessLevel> encryptionAccessLevels = meterProtocolSecuritySupportAdapter.getEncryptionAccessLevels();

        // asserts
        assertThat(encryptionAccessLevels).hasSize(1);
        assertEquals(SimpleTestDeviceSecuritySupport.ENCRYPTION_DEVICE_ACCESS_LEVEL_ID, encryptionAccessLevels.get(0).getId());
    }

    @Test
    public void setSecurityPropertySetTest() {
        PropertiesAdapter propertiesAdapter = mock(PropertiesAdapter.class);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);

        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        MeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter = new MeterProtocolSecuritySupportAdapter(
                simpleTestMeterProtocol,
                this.propertySpecService,
                this.protocolPluggableService,
                propertiesAdapter,
                this.securitySupportAdapterMappingFactory);

        // business method
        meterProtocolSecuritySupportAdapter.setSecurityPropertySet(deviceProtocolSecurityPropertySet);

        // asserts
        verify(propertiesAdapter, times(1)).copyProperties(Matchers.<TypedProperties>any());
    }

}