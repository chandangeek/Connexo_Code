package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageResultExecutor;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleLegacyMessageConverter;
import com.energyict.mdc.protocol.pluggable.mocks.DeviceMessageTestSpec;
import org.fest.assertions.core.Condition;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link SmartMeterProtocolMessageAdapter} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 9:50
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartMeterProtocolMessageAdapterTest {

    final String exceptionMessage = "Someone payed me 50€ to throw this IOException...";

    @Mock
    private DataModel dataModel;
    @Mock
    private MessageAdapterMappingFactory messageAdapterMappingFactory;
    @Mock
    private DeviceProtocolMessageService deviceProtocolMessageService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    private IssueServiceImpl issueService;

    @Before
    public void initializeMocks() {
        when(deviceProtocolMessageService.createDeviceProtocolMessagesFor(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                String javaClassName = (String) invocationOnMock.getArguments()[0];
                try {
                    return Class.forName(javaClassName).newInstance();
                }
                catch (ClassNotFoundException e) {
                    throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceMessageConverterClass(e, javaClassName);
                }
            }
        });
    }

    @Before
    public void initializeIssueService() {
        issueService = new IssueServiceImpl();
        issueService.setClock(new DefaultClock());
        com.energyict.mdc.issues.Bus.setIssueService(issueService);
    }

    @After
    public void cleanupIssueService () {
        com.energyict.mdc.issues.Bus.clearIssueService(issueService);
    }

    @Before
    public void before() {
        when(messageAdapterMappingFactory.getMessageMappingJavaClassNameForDeviceProtocol(
                "com.energyict.comserver.adapters.smartmeterprotocol.SimpleTestSmartMeterProtocol"))
                .thenReturn("com.energyict.comserver.adapters.common.SimpleLegacyMessageConverter");
        when(messageAdapterMappingFactory.getMessageMappingJavaClassNameForDeviceProtocol(
                "com.energyict.comserver.adapters.smartmeterprotocol.SecondSimpleTestSmartMeterProtocol"))
                .thenReturn("com.energyict.comserver.adapters.smartmeterprotocol.Certainly1NotKnown2ToThisClass3PathLegacyConverter");
        when(messageAdapterMappingFactory.getMessageMappingJavaClassNameForDeviceProtocol(
                "com.energyict.comserver.adapters.smartmeterprotocol.ThirdSimpleTestSmartMeterProtocol"))
                .thenReturn("com.energyict.comserver.adapters.smartmeterprotocol.ThirdSimpleTestSmartMeterProtocol");
    }

    @Test
    public void testKnownMeterProtocol() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, this.dataModel, this.protocolPluggableService);

        // all is safe if no errors occur
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownMeterProtocol() {
        SmartMeterProtocol meterProtocol = mock(SmartMeterProtocol.class, withSettings().extraInterfaces(MessageProtocol.class));
        try {
            new SmartMeterProtocolMessageAdapter(meterProtocol, this.dataModel, this.protocolPluggableService);
        } catch (DeviceProtocolAdapterCodingExceptions e) {
            if (!"CSC-DEV-124".equals(e.getMessageId())) {
                fail("Exception should have indicated that the given smartMeterProtocol is not known in the adapter mapping, but was " + e.getMessage());
            }
            throw e;
        }
        // should have gotten the exception
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownLegacyMessageConverterClass() {
        SmartMeterProtocol meterProtocol = new SecondSimpleTestSmartMeterProtocol();
        try {
            new SmartMeterProtocolMessageAdapter(meterProtocol, this.dataModel, this.protocolPluggableService);
        } catch (DeviceProtocolAdapterCodingExceptions e) {
            if (!"CSC-DEV-132".equals(e.getMessageId())) {
                fail("Exception should have indicated that the LegacyMessageConverter class is not known on the classpath, but was " + e.getMessage());
            }
            throw e;
        }
        // should have gotten the exception
    }

    @Test
    public void testNotALegacyClass() {
        SmartMeterProtocol meterProtocol = new ThirdSimpleTestSmartMeterProtocol();
        final SmartMeterProtocolMessageAdapter protocolMessageAdapter = new SmartMeterProtocolMessageAdapter(meterProtocol, this.dataModel, this.protocolPluggableService);

        assertThat(protocolMessageAdapter.executePendingMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(CollectedMessageList.class);
        assertThat(protocolMessageAdapter.updateSentMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(CollectedMessageList.class);
        assertThat(protocolMessageAdapter.format(null, null)).isEqualTo("");

        assertThat(protocolMessageAdapter.getSupportedMessages()).isEmpty();
    }

    @Test
    public void testGetSupportedMessages() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, this.dataModel, this.protocolPluggableService);

        // business method
        List<DeviceMessageSpec> supportedMessages = messageAdapter.getSupportedMessages();

        // asserts
        assertThat(supportedMessages).isNotNull();
        assertThat(supportedMessages).containsOnly(
                DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS,
                DeviceMessageTestSpec.TEST_SPEC_WITH_SIMPLE_SPECS);
    }

    @Test
    public void formatTest() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, this.dataModel, this.protocolPluggableService);
        PropertySpec mockPropertySpec = mock(PropertySpec.class);
        Code simpleCodeTable = mock(Code.class);
        Date simpleDate = new Date();

        // business method
        final String codeFormat = messageAdapter.format(mockPropertySpec, simpleCodeTable);
        final String dateFormat = messageAdapter.format(mockPropertySpec, simpleDate);

        // asserts
        assertThat(codeFormat).isEqualTo(SimpleLegacyMessageConverter.codeTableFormattingResult);
        assertThat(dateFormat).isEqualTo(SimpleLegacyMessageConverter.dateFormattingResult);
    }

    @Test
    public void testOnlyUpdateSentMessages() throws IOException {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, this.dataModel, this.protocolPluggableService);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);

        // business method
        final CollectedMessageList collectedMessageList = messageAdapter.updateSentMessages(Arrays.asList(offlineDeviceMessage));

        // asserts - we only call the protocol methods when both DeviceMessageSupport calls are made
        assertThat(CollectedMessageList.class.isAssignableFrom(collectedMessageList.getClass())).isTrue();
    }

    @Test
    public void testOnlyExecutePending() throws IOException {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, this.dataModel, this.protocolPluggableService);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);

        // business method
        final CollectedMessageList collectedMessageList = messageAdapter.executePendingMessages(Arrays.asList(offlineDeviceMessage));

        // asserts - we only call the protocol methods when both DeviceMessageSupport calls are made
        assertThat(CollectedMessageList.class.isAssignableFrom(collectedMessageList.getClass())).isTrue();
    }

    @Test
    public void testDelegationToMessageProtocol() throws IOException {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        simpleTestMeterProtocol.setQueryMessageResultExecutors(Arrays.asList(getNewSuccessMessageResultExecutor()));
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, this.dataModel, this.protocolPluggableService);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);

        // business method
        final CollectedMessageList CollectedMessageList = messageAdapter.executePendingMessages(Arrays.asList(offlineDeviceMessage));
        final CollectedMessageList collectedMessageList = messageAdapter.updateSentMessages(Arrays.asList(offlineDeviceMessage));

        // asserts
        assertThat(CollectedMessageList.class.isAssignableFrom(CollectedMessageList.getClass())).isTrue();
        assertThat(CollectedMessageList.class.isAssignableFrom(collectedMessageList.getClass())).isFalse();
        assertThat(collectedMessageList.getCollectedMessages()).hasSize(2);
        assertThat(collectedMessageList.getCollectedMessages().get(0).getNewDeviceMessageStatus()).isEqualTo(DeviceMessageStatus.CONFIRMED);
        assertThat(collectedMessageList.getResultType()).isEqualTo(ResultType.Supported);
    }

    @Test
    public void testIOExceptionDuringUpdateSentMessages() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        simpleTestMeterProtocol.setQueryMessageResultExecutors(Arrays.asList(getNewSuccessMessageResultExecutor()));
        simpleTestMeterProtocol.setApplyMessageResultExecutor(getNewIOExceptionMessageResultExecutor());
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, this.dataModel, this.protocolPluggableService);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);

        // business method
        final CollectedMessageList CollectedMessageList = messageAdapter.executePendingMessages(Arrays.asList(offlineDeviceMessage));
        final CollectedMessageList collectedMessageList = messageAdapter.updateSentMessages(Arrays.asList(offlineDeviceMessage));

        // asserts
        assertThat(CollectedMessageList.class.isAssignableFrom(CollectedMessageList.getClass())).isTrue();
        assertThat(CollectedMessageList.class.isAssignableFrom(collectedMessageList.getClass())).isFalse();
        assertThat(collectedMessageList.getCollectedMessages()).hasSize(0);
        assertThat(collectedMessageList.getResultType()).isEqualTo(ResultType.Other);
        assertThat(collectedMessageList.getIssues().get(0).isProblem()).isTrue();
        assertThat(collectedMessageList.getIssues().get(0).getDescription()).isEqualTo(Environment.DEFAULT.get().getTranslation("messageadapter.applymessages.issue"));
    }

    @Test
    public void testIOExceptionDuringSendingOfPendingMessages() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        simpleTestMeterProtocol.setQueryMessageResultExecutors(Arrays.asList(getNewSuccessMessageResultExecutor(), getNewIOExceptionMessageResultExecutor(), getNewSuccessMessageResultExecutor()));
        simpleTestMeterProtocol.setApplyMessageResultExecutor(getNewSuccessMessageResultExecutor());
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, this.dataModel, this.protocolPluggableService);
        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessage offlineDeviceMessage3 = mock(OfflineDeviceMessage.class);

        // business method
        final CollectedMessageList CollectedMessageList = messageAdapter.executePendingMessages(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2, offlineDeviceMessage3));
        final CollectedMessageList collectedMessageList = messageAdapter.updateSentMessages(Collections.<OfflineDeviceMessage>emptyList());

        // asserts
        assertThat(CollectedMessageList.class.isAssignableFrom(CollectedMessageList.getClass())).isTrue();
        assertThat(CollectedMessageList.class.isAssignableFrom(collectedMessageList.getClass())).isFalse();
        assertThat(collectedMessageList.getCollectedMessages()).hasSize(3);
        assertThat(collectedMessageList.getCollectedMessages()).has(new Condition<List<CollectedMessage>>() {
            @Override
            public boolean matches(List<CollectedMessage> collectedMessages) {
                int counter = 0;
                for (CollectedMessage collectedMessage : collectedMessages) {
                    if (!collectedMessage.getNewDeviceMessageStatus().equals(DeviceMessageStatus.CONFIRMED)) {
                        counter++;  // need to match that one of the messages resulted in an error
                    }
                }
                return counter == 1;
            }
        });
        assertThat(collectedMessageList.getCollectedMessages()).has(new Condition<List<CollectedMessage>>() {
            /**
             * Need to check if the failing message has the proper protocolInfo
             */
            @Override
            public boolean matches(List<CollectedMessage> collectedMessages) {
                boolean correctMessage = false;
                for (CollectedMessage collectedMessage : collectedMessages) {
                    if (collectedMessage.getNewDeviceMessageStatus().equals(DeviceMessageStatus.FAILED)) {
                        correctMessage = collectedMessage.getDeviceProtocolInformation().equals(exceptionMessage);
                    }
                }
                return correctMessage;
            }
        });
        assertThat(collectedMessageList.getResultType()).isEqualTo(ResultType.Supported);
    }

    private MessageResultExecutor getNewSuccessMessageResultExecutor() {
        return new MessageResultExecutor() {
            @Override
            public MessageResult performMessageResult() {
                return MessageResult.createSuccess(null);
            }
        };
    }

    private MessageResultExecutor getNewIOExceptionMessageResultExecutor() {
        return new MessageResultExecutor() {
            @Override
            public MessageResult performMessageResult() throws IOException {
                throw new IOException(exceptionMessage);
            }
        };
    }
}
