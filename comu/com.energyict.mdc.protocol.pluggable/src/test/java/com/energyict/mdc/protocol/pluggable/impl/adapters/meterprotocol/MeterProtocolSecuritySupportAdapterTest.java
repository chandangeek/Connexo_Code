package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.BasicPropertySpec;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecurityProperties;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleTestDeviceSecuritySupport;

import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
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
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName(), false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName(), false, new StringFactory()));
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.SECOND.javaName(), false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.SECOND.javaName(), false, new StringFactory()));
        when(propertySpecService.basicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.THIRD.javaName(), false, StringFactory.class))
                .thenReturn(new BasicPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.THIRD.javaName(), false, new StringFactory()));
        SimpleTestDeviceSecuritySupport securitySupport = new SimpleTestDeviceSecuritySupport(propertySpecService);
        when(this.protocolPluggableService.createDeviceProtocolSecurityFor(SimpleTestDeviceSecuritySupport.class.getName())).thenReturn(securitySupport);
        doThrow(ProtocolCreationException.class).when(this.protocolPluggableService).
                createDeviceProtocolSecurityFor("com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.NotAKnownDeviceSecuritySupportClass");
        when(this.protocolPluggableService.createDeviceProtocolSecurityFor(ThirdSimpleTestMeterProtocol.class.getName())).thenReturn(new ThirdSimpleTestMeterProtocol());
    }

    @Before
    public void initializeEnvironment() {
        when(deviceProtocolSecurityService.createDeviceProtocolSecurityFor(anyString())).thenAnswer(invocationOnMock -> {
            String javaClassName = (String) invocationOnMock.getArguments()[0];
            return Class.forName(javaClassName).newInstance();
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
            if (!e.getMessageSeed().equals(MessageSeeds.NON_EXISTING_MAP_ELEMENT)) {
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
        List<PropertySpec> securityProperties = meterProtocolSecuritySupportAdapter.getSecurityPropertySpecs();

        // asserts
        assertThat(securityProperties).hasSize(3);
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
        assertThat(meterProtocolSecuritySupportAdapter.getSecurityPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.javaName())).isPresent();
        assertThat(meterProtocolSecuritySupportAdapter.getSecurityPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.SECOND.javaName())).isPresent();
        assertThat(meterProtocolSecuritySupportAdapter.getSecurityPropertySpec(SimpleTestDeviceSecurityProperties.ActualFields.THIRD.javaName())).isPresent();
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
        assertThat(authenticationAccessLevels.get(0).getId()).isEqualTo(SimpleTestDeviceSecuritySupport.AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID);
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
        assertThat(encryptionAccessLevels.get(0).getId()).isEqualTo(SimpleTestDeviceSecuritySupport.ENCRYPTION_DEVICE_ACCESS_LEVEL_ID);
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