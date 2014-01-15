package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactoryProvider;
import com.energyict.comserver.adapters.common.SimpleLegacyMessageConverter;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.comserver.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.meterdata.NoOpCollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.services.impl.ServiceLocator;
import com.energyict.protocol.MessageProtocol;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link MeterProtocolMessageAdapter} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 12:05
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterProtocolMessageAdapterTest {

    @Mock
    private MessageAdapterMappingFactory messageAdapterMappingFactory;
    @Mock
    private Environment environment;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private LegacySecurityPropertyConverter legacySecurityPropertyConverter;
    @Mock
    private DeviceProtocolMessageService deviceProtocolMessageService;

    @Before
    public void before() {
        MessageAdapterMappingFactoryProvider messageAdapterMappingFactoryProvider = mock(MessageAdapterMappingFactoryProvider.class);
        when(messageAdapterMappingFactoryProvider.getMessageAdapterMappingFactory()).thenReturn(messageAdapterMappingFactory);
        MessageAdapterMappingFactoryProvider.INSTANCE.set(messageAdapterMappingFactoryProvider);
        when(messageAdapterMappingFactory.getMessageMappingJavaClassNameForDeviceProtocol(
                "com.energyict.comserver.adapters.meterprotocol.SimpleTestMeterProtocol"))
                .thenReturn("com.energyict.comserver.adapters.common.SimpleLegacyMessageConverter");
        when(messageAdapterMappingFactory.getMessageMappingJavaClassNameForDeviceProtocol(
                "com.energyict.comserver.adapters.meterprotocol.SecondSimpleTestMeterProtocol"))
                .thenReturn("com.energyict.comserver.adapters.meterprotocol.Certainly1NotKnown2ToThisClass3PathLegacyConverter");
        when(messageAdapterMappingFactory.getMessageMappingJavaClassNameForDeviceProtocol(
                "com.energyict.comserver.adapters.meterprotocol.ThirdSimpleTestMeterProtocol"))
                .thenReturn("com.energyict.comserver.adapters.meterprotocol.ThirdSimpleTestMeterProtocol");
    }

    @Before
    public void setUpServiceLocator() {
        com.energyict.mdc.services.impl.Bus.setServiceLocator(this.serviceLocator);
        when(this.serviceLocator.getDeviceProtocolMessageService()).thenReturn(this.deviceProtocolMessageService);
        when(this.deviceProtocolMessageService.createDeviceProtocolMessagesFor("com.energyict.comserver.adapters.common.SimpleLegacyMessageConverter")).
                thenReturn(new SimpleLegacyMessageConverter());
        doThrow(CodingException.class).when(this.deviceProtocolMessageService).createDeviceProtocolMessagesFor("com.energyict.comserver.adapters.meterprotocol.Certainly1NotKnown2ToThisClass3PathLegacyConverter");
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

    @After
    public void tearDownServiceLocator() {
        com.energyict.mdc.services.impl.Bus.clearServiceLocator(this.serviceLocator);
    }

    @Test
    public void testKnownMeterProtocol() {
        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        new MeterProtocolMessageAdapter(simpleTestMeterProtocol);

        // all is safe if no errors occur
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownMeterProtocol() {
        MeterProtocol meterProtocol = mock(MeterProtocol.class, withSettings().extraInterfaces(MessageProtocol.class));
        try {
            new MeterProtocolMessageAdapter(meterProtocol);
        } catch (DeviceProtocolAdapterCodingExceptions e) {
            if (!e.getMessageId().equals("CSC-DEV-124")) {
                fail("Exception should have indicated that the given MeterProtocol is not known in the adapter mapping, but was " + e.getMessage());
            }
            throw e;
        }
        // should have gotten the exception
    }

    @Test
    public void testNotAMessageSupportClass() {
        MeterProtocol meterProtocol = new ThirdSimpleTestMeterProtocol();
        final MeterProtocolMessageAdapter protocolMessageAdapter = new MeterProtocolMessageAdapter(meterProtocol);

        assertThat(protocolMessageAdapter.executePendingMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(NoOpCollectedMessageList.class);
        assertThat(protocolMessageAdapter.updateSentMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(NoOpCollectedMessageList.class);
        assertThat(protocolMessageAdapter.format(null, null)).isEqualTo("");

        assertThat(protocolMessageAdapter.getSupportedMessages()).isEmpty();
    }

}
