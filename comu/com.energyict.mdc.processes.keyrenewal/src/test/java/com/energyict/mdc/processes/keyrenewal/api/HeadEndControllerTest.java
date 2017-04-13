/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api;

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
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HeadEndControllerTest {

   /* private static final TranslationKey CONTACTOR_ACTIVATION_DATE_ATTRIBUTE_TRANSLATION_KEY = new TranslationKey() {
        @Override
        public String getKey() {
            return DeviceMessageConstants.contactorActivationDateAttributeName;
        }

        @Override
        public String getDefaultFormat() {
            return "Activation date";
        }
    };*/

    private static final String END_DEVICE_MRID = "endDeviceMRID";
    private static final String END_DEVICE_CONTROL_TYPE = "Renew key";
    private static final Long SERVICE_CALL_ID = 1L;

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    EndDevice endDevice;
    @Mock
    Device device;
    @Mock
    KeyAccessorType keyAccessorType;
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
    DestinationSpec destinationSpec;
    @Mock
    CompletionOptions completionOptions;

    ExceptionFactory exceptionFactory;
    private HeadEndController headEndController;
    private PropertySpecService propertySpecService;
    private PkiService pkiService;

    @Before
    public void setUp() throws Exception {
        setUpThesaurus();
        exceptionFactory = new ExceptionFactory(thesaurus);
        headEndController = new HeadEndController(messageService, exceptionFactory);

        when(endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
        when(endDevice.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));
        when(headEndInterface.getCommandFactory()).thenReturn(commandFactory);

        when(endDeviceCommand.getEndDeviceControlType()).thenReturn(endDeviceControlType);
        when(endDeviceControlType.getName()).thenReturn(END_DEVICE_CONTROL_TYPE);

        when(messageService.getDestinationSpec(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION)).thenReturn(Optional.of(destinationSpec));
        when(headEndInterface.sendCommand(any(EndDeviceCommand.class), any(Instant.class), any(ServiceCall.class))).thenReturn(completionOptions);
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
        deviceCommandInfo.keyAccessorType = "test";


        when(commandFactory.createKeyRenewalCommand(endDevice, keyAccessorType)).thenReturn(endDeviceCommand);
        //TODO change property
        /*PropertySpec keyAccessorTypeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(CONTACTOR_ACTIVATION_DATE_ATTREIBUTE_TRANSLATION_KEY)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();*/
      //  when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.singletonList(keyAccessorTypeSpec));

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, device);

        // Asserts
        verify(commandFactory).createConnectCommand(endDevice, Instant.now());
        verify(headEndInterface).sendCommand(endDeviceCommand, Instant.now(), serviceCall);
        verify(completionOptions).whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), destinationSpec);
    }


    @Test
    @Expected(value = LocalizedException.class, message = "Could not find the command argument spec " + DeviceMessageConstants.overThresholdDurationAttributeName + " for command " + END_DEVICE_CONTROL_TYPE)
    public void testCommandArgumentSpecNotFound() throws Exception {
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();


        when(commandFactory.createConnectCommand(endDevice, Instant.now())).thenReturn(endDeviceCommand);
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.emptyList());

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, device);
    }

    @Test
    @Expected(value = LocalizedException.class, message = "Could not find destination spec with name " + CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION)
    public void testDestinationSpecNotFound() throws Exception {
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();

        when(messageService.getDestinationSpec(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION)).thenReturn(Optional.empty());

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, device);
    }
}