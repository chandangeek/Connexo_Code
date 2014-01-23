package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleLegacyMessageConverter;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
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
    private LegacySecurityPropertyConverter legacySecurityPropertyConverter;
    @Mock
    private DeviceProtocolMessageService deviceProtocolMessageService;

    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    private NlsService nlsService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private EventService eventService;
    @Mock
    private PluggableService pluggableService;
    @Mock
    private RelationService relationService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    @Mock
    private ConnectionTypeService connectionTypeService;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;

    private ProtocolPluggableService protocolPluggableService;

    @Before
    public void initializeMocks () {
        when(this.deviceProtocolMessageService.createDeviceProtocolMessagesFor("com.energyict.comserver.adapters.common.SimpleLegacyMessageConverter")).
                thenReturn(new SimpleLegacyMessageConverter());
        doThrow(DeviceProtocolAdapterCodingExceptions.class).when(this.deviceProtocolMessageService).createDeviceProtocolMessagesFor("com.energyict.comserver.adapters.meterprotocol.Certainly1NotKnown2ToThisClass3PathLegacyConverter");
        protocolPluggableService =
                new ProtocolPluggableServiceImpl(
                        this.ormService,
                        this.eventService,
                        this.nlsService,
                        this.propertySpecService,
                        this.pluggableService,
                        this.relationService,
                        this.deviceProtocolService,
                        this.deviceProtocolMessageService,
                        this.deviceProtocolSecurityService,
                        this.inboundDeviceProtocolService,
                        this.connectionTypeService);
    }

    @Before
    public void before() {
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

    @Test
    public void testKnownMeterProtocol() {
        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        new MeterProtocolMessageAdapter(simpleTestMeterProtocol, this.dataModel, this.protocolPluggableService);

        // all is safe if no errors occur
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownMeterProtocol() {
        MeterProtocol meterProtocol = mock(MeterProtocol.class, withSettings().extraInterfaces(MessageProtocol.class));
        try {
            new MeterProtocolMessageAdapter(meterProtocol, this.dataModel, this.protocolPluggableService);
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
        final MeterProtocolMessageAdapter protocolMessageAdapter = new MeterProtocolMessageAdapter(meterProtocol, this.dataModel, this.protocolPluggableService);

        assertThat(protocolMessageAdapter.executePendingMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(CollectedMessageList.class);
        assertThat(protocolMessageAdapter.updateSentMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(CollectedMessageList.class);
        assertThat(protocolMessageAdapter.format(null, null)).isEqualTo("");

        assertThat(protocolMessageAdapter.getSupportedMessages()).isEmpty();
    }

}
