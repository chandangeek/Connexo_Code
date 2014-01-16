package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryImpl;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    private SecuritySupportAdapterMappingFactoryImpl securitySupportAdapterMappingFactory;
    @Mock
    private static UserEnvironment userEnvironment = mock(UserEnvironment.class);
    @Mock
    private Environment environment;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;

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
                    return Class.forName(javaClassName).newInstance();
                }
        });
    }

    @After
    public void cleanupEnvironment() {
        Environment.DEFAULT.set(null);
    }

    @Before
    public void before() {
        when(securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol
                ("com.energyict.comserver.adapters.smartmeterprotocol.SimpleTestSmartMeterProtocol")).thenReturn("com.energyict.comserver.adapters.common.SimpleTestDeviceSecuritySupport");
        when(securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol
                ("com.energyict.comserver.adapters.smartmeterprotocol.SecondSimpleTestSmartMeterProtocol")).thenReturn("com.energyict.comserver.adapters.smartmeterprotocol.NotAKnownDeviceSecuritySupportClass");
        when(securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol
                ("com.energyict.comserver.adapters.smartmeterprotocol.ThirdSimpleTestSmartMeterProtocol")).thenReturn("com.energyict.comserver.adapters.smartmeterprotocol.ThirdSimpleTestSmartMeterProtocol");
    }

    @Test
    public void testKnownMeterProtocol() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        new SmartMeterProtocolSecuritySupportAdapter(simpleTestMeterProtocol, mock(PropertiesAdapter.class), this.securitySupportAdapterMappingFactory);

        // all is safe if no errors occur
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownMeterProtocol() {
        SmartMeterProtocol meterProtocol = mock(SmartMeterProtocol.class);
        try {
            new SmartMeterProtocolSecuritySupportAdapter(meterProtocol, mock(PropertiesAdapter.class), this.securitySupportAdapterMappingFactory);
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
        SmartMeterProtocol meterProtocol = new ThirdSimpleTestSmartMeterProtocol();
        final SmartMeterProtocolSecuritySupportAdapter spy = spy(new SmartMeterProtocolSecuritySupportAdapter(meterProtocol, mock(PropertiesAdapter.class), this.securitySupportAdapterMappingFactory));

        // asserts
        verify(spy, never()).setLegacySecuritySupport(any(DeviceProtocolSecurityCapabilities.class));
        verify(spy, never()).setLegacySecurityPropertyConverter(any(LegacySecurityPropertyConverter.class));
    }

    @Test
    public void setSecurityPropertySetTest() {
        PropertiesAdapter propertiesAdapter = mock(PropertiesAdapter.class);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);

        SimpleTestSmartMeterProtocol simpleTestSmartMeterProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter = new SmartMeterProtocolSecuritySupportAdapter(simpleTestSmartMeterProtocol, propertiesAdapter, this.securitySupportAdapterMappingFactory);

        // business method
        meterProtocolSecuritySupportAdapter.setSecurityPropertySet(deviceProtocolSecurityPropertySet);

        // asserts
        verify(propertiesAdapter, times(1)).copyProperties(Matchers.<TypedProperties>any());
    }

}