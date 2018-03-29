/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl;

import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.processes.keyrenewal.api.impl.Command;
import com.energyict.mdc.processes.keyrenewal.api.impl.CompletionOptionsMessageHandlerFactory;
import com.energyict.mdc.processes.keyrenewal.api.impl.DeviceCommandInfo;
import com.energyict.mdc.processes.keyrenewal.api.impl.HeadEndController;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HeadEndControllerTest {

    private static final TranslationKey KEY_ACCESSORTYPE_ATTRIBUTE_TRANSLATION_KEY = new TranslationKey() {
        @Override
        public String getKey() {
            return "keyAccessorType";
        }

        @Override
        public String getDefaultFormat() {
            return "keyAccessorType";
        }
    };

    private static final String END_DEVICE_MRID = "endDeviceMRID";
    private static final String END_DEVICE_CONTROL_TYPE = "Renew key";
    private static final String KEY_ACCESSOR_TYPE = "AK";
    private static final Long SERVICE_CALL_ID = 1L;

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    EndDevice endDevice;
    @Mock
    Device device;
    @Mock
    SecurityAccessorType securityAccessorType;
    @Mock
    ServiceCall serviceCall;
    @Mock
    Thesaurus thesaurus;
    @Mock
    HeadEndInterface headEndInterface;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    CommandFactory commandFactory;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private BeanService beanService;
    @Mock
    EndDeviceCommand endDeviceCommand;
    @Mock
    EndDeviceControlType endDeviceControlType;
    @Mock
    MessageService messageService;
    @Mock
    JsonService jsonService;
    @Mock
    SecurityManagementService securityManagementService;
    @Mock
    CaService caService;
    @Mock
    TransactionService transactionService;
    @Mock
    ServiceCallService serviceCallService;
    @Mock
    DestinationSpec destinationSpec;
    @Mock
    CompletionOptions completionOptions;
    @Mock
    MultiSenseHeadEndInterface multiSenseHeadEndInterface;
    @Mock
    ComTaskExecution comTaskExecution;

    ExceptionFactory exceptionFactory;

    private HeadEndController headEndController;
    private PropertySpecService propertySpecService;


    @Before
    public void setUp() throws Exception {
        setUpThesaurus();
        exceptionFactory = new ExceptionFactory(thesaurus);
        headEndController = spy(new HeadEndController(messageService, exceptionFactory));

        when(endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
        when(endDevice.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));
        when(headEndInterface.getCommandFactory()).thenReturn(commandFactory);

        when(endDeviceCommand.getEndDeviceControlType()).thenReturn(endDeviceControlType);
        when(endDeviceControlType.getName()).thenReturn(END_DEVICE_CONTROL_TYPE);

        when(messageService.getDestinationSpec(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION)).thenReturn(Optional.of(destinationSpec));
        when(headEndInterface.sendCommand(any(EndDeviceCommand.class), any(Instant.class), any(ServiceCall.class))).thenReturn(completionOptions);
        when(multiSenseHeadEndInterface.runCommunicationTask(any(Device.class), anyList(), any(Instant.class), any(ServiceCall.class))).thenReturn(completionOptions);
        when(serviceCall.getId()).thenReturn(SERVICE_CALL_ID);

        this.propertySpecService = new PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService);
    }

    private void setUpThesaurus() {
        when(thesaurus.getFormat(any(TranslationKey.class))).thenAnswer(invocationOnMock -> {
            TranslationKey translationKey = (TranslationKey) invocationOnMock.getArguments()[0];
            return new NlsMessageFormat() {
                @Override
                public String format(Object... args) {
                    return MessageFormat.format(translationKey.getDefaultFormat(), args);
                }

                @Override
                public String format(Locale locale, Object... args) {
                    return MessageFormat.format(translationKey.getDefaultFormat(), args);
                }
            };
        });
        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocationOnMock -> {
            MessageSeed messageSeed = (MessageSeed) invocationOnMock.getArguments()[0];
            return new NlsMessageFormat() {
                @Override
                public String format(Object... args) {
                    return MessageFormat.format(messageSeed.getDefaultFormat(), args);
                }

                @Override
                public String format(Locale locale, Object... args) {
                    return MessageFormat.format(messageSeed.getDefaultFormat(), args);
                }
            };
        });
    }

    @Test
    @Expected(value = LocalizedException.class, message = "Could not find the head-end interface for end device with MRID " + END_DEVICE_MRID)
    public void testInvalidDevice() throws Exception {
        when(endDevice.getHeadEndInterface()).thenReturn(Optional.empty());

        // Business method
        headEndController.performOperations(endDevice, serviceCall, new DeviceCommandInfo(), device);
    }

    @Test
    public void testRenewKeyOperation() throws Exception {
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();
        deviceCommandInfo.keyAccessorType = KEY_ACCESSOR_TYPE;
        deviceCommandInfo.activationDate = Instant.now();
        deviceCommandInfo.command = Command.RENEW_KEY;

        Mockito.doReturn(securityAccessorType).when(headEndController).getKeyAccessorType(KEY_ACCESSOR_TYPE, device);

        when(commandFactory.createKeyRenewalCommand(endDevice, securityAccessorType)).thenReturn(endDeviceCommand);
        PropertySpec keyAccessorTypeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(KEY_ACCESSORTYPE_ATTRIBUTE_TRANSLATION_KEY)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.singletonList(keyAccessorTypeSpec));

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, device);

        // Asserts
        verify(commandFactory).createKeyRenewalCommand(endDevice, securityAccessorType);
        verify(headEndInterface).sendCommand(endDeviceCommand, deviceCommandInfo.activationDate, serviceCall);
        verify(completionOptions).whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), destinationSpec);
    }

    @Ignore
    @Test
    @Expected(value = LocalizedException.class, message = "Could not find destination spec with name " + CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION)
    public void testDestinationSpecNotFound() throws Exception {
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();
        deviceCommandInfo.keyAccessorType = KEY_ACCESSOR_TYPE;
        deviceCommandInfo.activationDate = Instant.now();

        Mockito.doReturn(securityAccessorType).when(headEndController).getKeyAccessorType(KEY_ACCESSOR_TYPE, device);

        when(messageService.getDestinationSpec(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION)).thenReturn(Optional.empty());

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, device);
    }

    @Test
    public void testCommunicationTestOperation() throws Exception {
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();
        deviceCommandInfo.keyAccessorType = KEY_ACCESSOR_TYPE;
        deviceCommandInfo.activationDate = Instant.now();

        List<ComTaskExecution> comtasks = new ArrayList<>();
        comtasks.add(comTaskExecution);

        when(endDevice.getHeadEndInterface()).thenReturn(Optional.of(multiSenseHeadEndInterface));

        Mockito.doReturn(securityAccessorType).when(headEndController).getKeyAccessorType(KEY_ACCESSOR_TYPE, device);
        Mockito.doReturn(comtasks).when(headEndController).getComTaskExecutions(device, securityAccessorType);
        Mockito.doReturn(comtasks).when(headEndController).getFilteredList(comtasks);


        // Business method
        headEndController.performTestCommunication(endDevice, serviceCall, deviceCommandInfo, device);

        // Asserts
        verify(multiSenseHeadEndInterface).runCommunicationTask(device, comtasks, deviceCommandInfo.activationDate, serviceCall);
        verify(completionOptions).whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), destinationSpec);
    }
}