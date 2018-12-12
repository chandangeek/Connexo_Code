/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.impl.DataModelInitializer;
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageResultExecutor;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleLegacyMessageConverter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks.MockCollectedMessage;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks.MockCollectedMessageList;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import org.fest.assertions.core.Condition;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link SmartMeterProtocolMessageAdapter} component.
 * <p>
 * Date: 11/03/13
 * Time: 9:50
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartMeterProtocolMessageAdapterTest {

    final String exceptionMessage = "Someone payed me 50€ to throw this IOException...";

    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;

    private InMemoryPersistence inMemoryPersistence;
    private ProtocolPluggableServiceImpl protocolPluggableService;
    private DataModel dataModel;
    @Mock
    private PropertySpecService propertySpecService;

    @Before
    public void initializeDatabaseAndMocks() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase(
                "SmartMeterProtocolMessageAdapterTest.mdc.protocol.pluggable",
                new DataModelInitializer() {
                    @Override
                    public void initializeDataModel(DataModel dataModel) {
                        dataModel.persist(new MessageAdapterMappingImpl(SimpleTestSmartMeterProtocol.class.getName(), SimpleLegacyMessageConverter.class.getName()));
                        dataModel.persist(new MessageAdapterMappingImpl(SecondSimpleTestSmartMeterProtocol.class.getName(), "com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.Certainly1NotKnown2ToThisClass3PathLegacyConverter"));
                        dataModel.persist(new MessageAdapterMappingImpl(ThirdSimpleTestSmartMeterProtocol.class.getName(), ThirdSimpleTestSmartMeterProtocol.class.getName()));
                    }
                });
        this.protocolPluggableService = this.inMemoryPersistence.getProtocolPluggableService();
        this.dataModel = this.protocolPluggableService.getDataModel();
        this.initializeMocks();
    }

    private void initializeMocks() {
        DeviceProtocolMessageService deviceProtocolMessageService = this.inMemoryPersistence.getDeviceProtocolMessageService();
        when(deviceProtocolMessageService.createDeviceProtocolMessagesFor(SimpleLegacyMessageConverter.class.getCanonicalName())).thenReturn(new SimpleLegacyMessageConverter());
        doThrow(DeviceProtocolAdapterCodingExceptions.class).when(deviceProtocolMessageService).createDeviceProtocolMessagesFor("com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.Certainly1NotKnown2ToThisClass3PathLegacyConverter");
        when(deviceProtocolMessageService.createDeviceProtocolMessagesFor(ThirdSimpleTestSmartMeterProtocol.class.getCanonicalName())).thenReturn(new ThirdSimpleTestSmartMeterProtocol());

        when(this.collectedDataFactory.createCollectedMessageList(anyList())).thenReturn(new MockCollectedMessageList());
        when(this.collectedDataFactory.createCollectedMessage(any(MessageIdentifier.class))).thenAnswer(
                invocationOnMock -> new MockCollectedMessage((MessageIdentifier) invocationOnMock.getArguments()[0])
        );
        when(this.collectedDataFactory.createEmptyCollectedMessageList()).thenReturn(new MockCollectedMessageList());
        TariffCalendarExtractor.ThreadContext threadContext = mock(TariffCalendarExtractor.ThreadContext.class);
        TariffCalendarExtractor tariffCalendarExtractor = mock(TariffCalendarExtractor.class);
        when(tariffCalendarExtractor.threadContext()).thenReturn(threadContext);
        Services.tariffCalendarExtractor(tariffCalendarExtractor);
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void testKnownMeterProtocol() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);

        // all is safe if no errors occur
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownMeterProtocol() {
        SmartMeterProtocol meterProtocol = mock(SmartMeterProtocol.class, withSettings().extraInterfaces(MessageProtocol.class));
        try {
            new SmartMeterProtocolMessageAdapter(meterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);
        } catch (DeviceProtocolAdapterCodingExceptions e) {
            assertThat(e.getMessageSeed().equals(MessageSeeds.NON_EXISTING_MAP_ELEMENT));
            throw e;
        }
        // should have gotten the exception
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownLegacyMessageConverterClass() {
        SmartMeterProtocol meterProtocol = new SecondSimpleTestSmartMeterProtocol();
        new SmartMeterProtocolMessageAdapter(meterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);

        // should have gotten the exception
    }

    @Test
    public void testNotALegacyClass() {
        SmartMeterProtocol meterProtocol = new ThirdSimpleTestSmartMeterProtocol();
        final SmartMeterProtocolMessageAdapter protocolMessageAdapter = new SmartMeterProtocolMessageAdapter(meterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);

        assertThat(protocolMessageAdapter.executePendingMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(CollectedMessageList.class);
        assertThat(protocolMessageAdapter.updateSentMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(CollectedMessageList.class);
        assertThat(protocolMessageAdapter.format(null, null, null, null)).isEqualTo("");

        assertThat(protocolMessageAdapter.getSupportedMessages()).isEmpty();
    }

    @Test
    public void testGetSupportedMessages() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);

        // business method
        List<DeviceMessageSpec> supportedMessages = messageAdapter.getSupportedMessages();

        // asserts
        assertThat(supportedMessages).isEmpty();
    }

    @Test
    public void formatTest() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);
        com.energyict.mdc.upl.properties.PropertySpec mockPropertySpec = mock(com.energyict.mdc.upl.properties.PropertySpec.class);
        Calendar simpleCodeTable = mock(Calendar.class);
        Date simpleDate = new Date();

        // business method
        final String codeFormat = messageAdapter.format(null, null, mockPropertySpec, simpleCodeTable);
        final String dateFormat = messageAdapter.format(null, null, mockPropertySpec, simpleDate);

        // asserts
        assertThat(codeFormat).isEqualTo(SimpleLegacyMessageConverter.calendarFormattingResult);
        assertThat(dateFormat).isEqualTo(SimpleLegacyMessageConverter.dateFormattingResult);
    }

    @Test
    public void testOnlyUpdateSentMessages() throws IOException {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);

        // business method
        final CollectedMessageList collectedMessageList = messageAdapter.updateSentMessages(Arrays.asList(offlineDeviceMessage));

        // asserts - we only call the protocol methods when both DeviceMessageSupport calls are made
        assertThat(CollectedMessageList.class.isAssignableFrom(collectedMessageList.getClass())).isTrue();
    }

    @Test
    public void testOnlyExecutePending() throws IOException {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);
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
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);

        // business method
        final CollectedMessageList pendingMessages = messageAdapter.executePendingMessages(Arrays.asList(offlineDeviceMessage));
        final CollectedMessageList sentMessages = messageAdapter.updateSentMessages(Arrays.asList(offlineDeviceMessage));

        // asserts
        assertThat(sentMessages.getCollectedMessages()).hasSize(2);
        assertThat(sentMessages.getCollectedMessages().get(0).getNewDeviceMessageStatus()).isEqualTo(DeviceMessageStatus.CONFIRMED);
        assertThat(sentMessages.getResultType()).isEqualTo(ResultType.Supported);
    }

    @Test
    public void testIOExceptionDuringUpdateSentMessages() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        simpleTestMeterProtocol.setQueryMessageResultExecutors(Arrays.asList(getNewSuccessMessageResultExecutor()));
        simpleTestMeterProtocol.setApplyMessageResultExecutor(getNewIOExceptionMessageResultExecutor());
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);

        // business method
        final CollectedMessageList pendingMessages = messageAdapter.executePendingMessages(Arrays.asList(offlineDeviceMessage));
        final CollectedMessageList sentMessages = messageAdapter.updateSentMessages(Arrays.asList(offlineDeviceMessage));

        // asserts
        assertThat(sentMessages.getCollectedMessages()).isEmpty();
        assertThat(sentMessages.getResultType()).isEqualTo(ResultType.Other);
        assertThat(sentMessages.getIssues().get(0).isProblem()).isTrue();
        assertThat(sentMessages.getIssues().get(0).getDescription()).isNotEmpty();
    }

    @Test
    public void testIOExceptionDuringSendingOfPendingMessages() {
        SimpleTestSmartMeterProtocol simpleTestMeterProtocol = new SimpleTestSmartMeterProtocol();
        simpleTestMeterProtocol.setQueryMessageResultExecutors(Arrays.asList(getNewSuccessMessageResultExecutor(), getNewIOExceptionMessageResultExecutor(), getNewSuccessMessageResultExecutor()));
        simpleTestMeterProtocol.setApplyMessageResultExecutor(getNewSuccessMessageResultExecutor());
        SmartMeterProtocolMessageAdapter messageAdapter = new SmartMeterProtocolMessageAdapter(simpleTestMeterProtocol, new MessageAdapterMappingFactoryImpl(this.dataModel), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory, this.deviceMessageSpecificationService);
        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        OfflineDeviceMessage offlineDeviceMessage3 = mock(OfflineDeviceMessage.class);

        // business method
        final CollectedMessageList pendingMessages = messageAdapter.executePendingMessages(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2, offlineDeviceMessage3));
        final CollectedMessageList sentMessages = messageAdapter.updateSentMessages(Collections.<OfflineDeviceMessage>emptyList());

        // asserts
        assertThat(pendingMessages.getCollectedMessages()).isEmpty();
        assertThat(pendingMessages.getIssues()).isEmpty();
        assertThat(sentMessages.getCollectedMessages()).hasSize(3);
        assertThat(sentMessages.getCollectedMessages()).has(new Condition<List<CollectedMessage>>() {
            @Override
            public boolean matches(List<CollectedMessage> collectedMessages) {
                int counter = 0;
                for (CollectedMessage collectedMessage : collectedMessages) {
                    if (!DeviceMessageStatus.CONFIRMED.equals(collectedMessage.getNewDeviceMessageStatus())) {
                        counter++;  // need to match that one of the messages resulted in an error
                    }
                }
                return counter == 1;
            }
        });
        assertThat(sentMessages.getCollectedMessages()).has(new Condition<List<CollectedMessage>>() {
            /**
             * Need to check if the failing message has the proper protocolInfo.
             */
            @Override
            public boolean matches(List<CollectedMessage> collectedMessages) {
                boolean correctMessage = false;
                for (CollectedMessage collectedMessage : collectedMessages) {
                    if (DeviceMessageStatus.FAILED.equals(collectedMessage.getNewDeviceMessageStatus())) {
                        correctMessage = collectedMessage.getDeviceProtocolInformation().equals(exceptionMessage);
                    }
                }
                return correctMessage;
            }
        });
        assertThat(sentMessages.getResultType()).isEqualTo(ResultType.Supported);
    }

    private MessageResultExecutor getNewSuccessMessageResultExecutor() {
        return () -> MessageResult.createSuccess(null);
    }

    private MessageResultExecutor getNewIOExceptionMessageResultExecutor() {
        return () -> {
            throw new IOException(exceptionMessage);
        };
    }
}
