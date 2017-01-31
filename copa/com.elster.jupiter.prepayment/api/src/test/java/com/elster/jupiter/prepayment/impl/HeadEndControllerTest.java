/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

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
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageAttributes;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * @author sva
 * @since 16/06/2016 - 16:56
 */
@RunWith(MockitoJUnitRunner.class)
public class HeadEndControllerTest {

    private static final TranslationKey CONTACTOR_ACTIVATION_DATE_ATTREIBUTE_TRANSLATION_KEY = new TranslationKey() {
        @Override
        public String getKey() {
            return DeviceMessageConstants.contactorActivationDateAttributeName;
        }

        @Override
        public String getDefaultFormat() {
            return "Activation date";
        }
    };

    private static final String END_DEVICE_MRID = "endDeviceMRID";
    private static final String END_DEVICE_CONTROL_TYPE = "RCDSwitch ArmForClosure";
    private static final Long SERVICE_CALL_ID = 1L;

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    EndDevice endDevice;
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
    public void testInvalidUsagePoint() throws Exception {
        when(endDevice.getHeadEndInterface()).thenReturn(Optional.empty());

        // Business method
        headEndController.performContactorOperations(endDevice, serviceCall, new ContactorInfo());
    }

    @Test
    public void testConnectBreakerOperation() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.status = BreakerStatus.connected;
        contactorInfo.activationDate = Instant.now();

        when(commandFactory.createConnectCommand(endDevice, contactorInfo.activationDate)).thenReturn(endDeviceCommand);
        PropertySpec dateTimeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(CONTACTOR_ACTIVATION_DATE_ATTREIBUTE_TRANSLATION_KEY)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.singletonList(dateTimeSpec));

        // Business method
        headEndController.performContactorOperations(endDevice, serviceCall, contactorInfo);

        // Asserts
        verify(commandFactory).createConnectCommand(endDevice, contactorInfo.activationDate);
        verify(headEndInterface).sendCommand(endDeviceCommand, contactorInfo.activationDate, serviceCall);
        verify(completionOptions).whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), destinationSpec);
    }

    @Test
    public void testDisconnectBreakerOperation() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.status = BreakerStatus.disconnected;
        contactorInfo.activationDate = Instant.now();

        when(commandFactory.createDisconnectCommand(endDevice, contactorInfo.activationDate)).thenReturn(endDeviceCommand);
        PropertySpec dateTimeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(CONTACTOR_ACTIVATION_DATE_ATTREIBUTE_TRANSLATION_KEY)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.singletonList(dateTimeSpec));

        // Business method
        headEndController.performContactorOperations(endDevice, serviceCall, contactorInfo);

        // Asserts
        verify(commandFactory).createDisconnectCommand(endDevice, contactorInfo.activationDate);
        verify(headEndInterface).sendCommand(endDeviceCommand, contactorInfo.activationDate, serviceCall);
        verify(completionOptions).whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), destinationSpec);
    }

    @Test
    public void testArmBreakerOperation() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.status = BreakerStatus.armed;
        contactorInfo.activationDate = Instant.now();

        when(commandFactory.createArmCommand(endDevice, false, contactorInfo.activationDate)).thenReturn(endDeviceCommand);
        PropertySpec dateTimeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(CONTACTOR_ACTIVATION_DATE_ATTREIBUTE_TRANSLATION_KEY)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.singletonList(dateTimeSpec));

        // Business method
        headEndController.performContactorOperations(endDevice, serviceCall, contactorInfo);

        // Asserts
        verify(commandFactory).createArmCommand(endDevice, false, contactorInfo.activationDate);
        verify(headEndInterface).sendCommand(endDeviceCommand, contactorInfo.activationDate, serviceCall);
        verify(completionOptions).whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), destinationSpec);
    }

    @Test
    public void testDisableLoadLimitingOperation() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.loadLimit = contactorInfo.new LoadLimit(BigDecimal.ZERO, null);

        when(commandFactory.createDisableLoadLimitCommand(endDevice)).thenReturn(endDeviceCommand);
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.emptyList());

        // Business method
        headEndController.performContactorOperations(endDevice, serviceCall, contactorInfo);

        // Asserts
        verify(commandFactory).createDisableLoadLimitCommand(endDevice);
        verify(endDeviceCommand, never()).setPropertyValue(any(), any());
        verify(headEndInterface).sendCommand(eq(endDeviceCommand), any(Instant.class), eq(serviceCall));
        verify(completionOptions).whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), destinationSpec);
    }

    @Test
    public void testEnableLoadLimittingOperation() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.loadLimit = contactorInfo.new LoadLimit(BigDecimal.TEN, "kWh");
        Quantity quantity = Quantity.create(BigDecimal.TEN, 3, "Wh");

        when(commandFactory.createEnableLoadLimitCommand(endDevice, quantity)).thenReturn(endDeviceCommand);
        List<PropertySpec> propertySpecs = new ArrayList<>();
        PropertySpec thresholdPropertySpec = propertySpecService
                .bigDecimalSpec()
                .named(DeviceMessageAttributes.normalThresholdAttributeName)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec unitPropertySpec = propertySpecService
                .stringSpec()
                .named(DeviceMessageAttributes.unitAttributeName)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec loadTolerancePropertySpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(DeviceMessageAttributes.overThresholdDurationAttributeName)
                .fromThesaurus(thesaurus)
                .finish();
        propertySpecs.add(thresholdPropertySpec);
        propertySpecs.add(unitPropertySpec);
        propertySpecs.add(loadTolerancePropertySpec);
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(propertySpecs);

        // Business method
        headEndController.performContactorOperations(endDevice, serviceCall, contactorInfo);

        // Asserts
        verify(commandFactory).createEnableLoadLimitCommand(endDevice, quantity);
        verify(endDeviceCommand, never()).setPropertyValue(eq(loadTolerancePropertySpec), any());
        verify(headEndInterface).sendCommand(eq(endDeviceCommand), any(Instant.class), eq(serviceCall));
        verify(completionOptions).whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), destinationSpec);
    }

    @Test
    public void testEnableFullLoadLimittingOperation() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.loadLimit = contactorInfo.new LoadLimit(BigDecimal.TEN, "kWh");
        contactorInfo.loadTolerance = 30;
        Quantity quantity = Quantity.create(BigDecimal.TEN, 3, "Wh");

        when(commandFactory.createEnableLoadLimitCommand(endDevice, quantity)).thenReturn(endDeviceCommand);
        List<PropertySpec> propertySpecs = new ArrayList<>();
        PropertySpec thresholdPropertySpec = propertySpecService
                .bigDecimalSpec()
                .named(DeviceMessageAttributes.normalThresholdAttributeName)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec unitPropertySpec = propertySpecService
                .stringSpec()
                .named(DeviceMessageAttributes.unitAttributeName)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec loadTolerancePropertySpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(DeviceMessageAttributes.overThresholdDurationAttributeName)
                .fromThesaurus(thesaurus)
                .finish();
        propertySpecs.add(thresholdPropertySpec);
        propertySpecs.add(unitPropertySpec);
        propertySpecs.add(loadTolerancePropertySpec);
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(propertySpecs);

        // Business method
        headEndController.performContactorOperations(endDevice, serviceCall, contactorInfo);

        // Asserts
        verify(commandFactory).createEnableLoadLimitCommand(endDevice, quantity);
        verify(endDeviceCommand).setPropertyValue(loadTolerancePropertySpec, TimeDuration.seconds(contactorInfo.loadTolerance));
        verify(headEndInterface).sendCommand(eq(endDeviceCommand), any(Instant.class), eq(serviceCall));
        verify(completionOptions).whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), destinationSpec);
    }

    @Test
    public void testConnectBreakerAndEnableFullLoadLimittingOperation() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.status = BreakerStatus.connected;
        contactorInfo.activationDate = Instant.now();
        contactorInfo.loadLimit = contactorInfo.new LoadLimit(BigDecimal.TEN, "kWh");
        contactorInfo.loadTolerance = 30;
        Quantity quantity = Quantity.create(BigDecimal.TEN, 3, "Wh");

        EndDeviceCommand breakerEndDeviceCommand = mock(EndDeviceCommand.class);
        EndDeviceCommand loadLimitEndDeviceCommand = mock(EndDeviceCommand.class);
        when(breakerEndDeviceCommand.getEndDeviceControlType()).thenReturn(endDeviceControlType);
        when(loadLimitEndDeviceCommand.getEndDeviceControlType()).thenReturn(endDeviceControlType);

        when(commandFactory.createConnectCommand(endDevice, contactorInfo.activationDate)).thenReturn(breakerEndDeviceCommand);
        when(commandFactory.createEnableLoadLimitCommand(endDevice, quantity)).thenReturn(loadLimitEndDeviceCommand);
        PropertySpec dateTimeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(CONTACTOR_ACTIVATION_DATE_ATTREIBUTE_TRANSLATION_KEY)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec thresholdPropertySpec = propertySpecService
                .bigDecimalSpec()
                .named(DeviceMessageAttributes.normalThresholdAttributeName)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec unitPropertySpec = propertySpecService
                .stringSpec()
                .named(DeviceMessageAttributes.unitAttributeName)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec loadTolerancePropertySpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(DeviceMessageAttributes.overThresholdDurationAttributeName)
                .fromThesaurus(thesaurus)
                .finish();
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(thresholdPropertySpec);
        propertySpecs.add(unitPropertySpec);
        propertySpecs.add(loadTolerancePropertySpec);
        when(breakerEndDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.singletonList(dateTimeSpec));
        when(loadLimitEndDeviceCommand.getCommandArgumentSpecs()).thenReturn(propertySpecs);

        // Business method
        headEndController.performContactorOperations(endDevice, serviceCall, contactorInfo);

        // Asserts
        verify(commandFactory).createEnableLoadLimitCommand(endDevice, quantity);
        verify(loadLimitEndDeviceCommand).setPropertyValue(loadTolerancePropertySpec, TimeDuration.seconds(contactorInfo.loadTolerance));
        verify(headEndInterface).sendCommand(breakerEndDeviceCommand, contactorInfo.activationDate, serviceCall);
        verify(headEndInterface).sendCommand(eq(loadLimitEndDeviceCommand), any(Instant.class), eq(serviceCall));
        verify(completionOptions, times(2)).whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()), destinationSpec);
    }

    @Test
    @Expected(value = LocalizedException.class, message = "Could not find the command argument spec " + DeviceMessageConstants.overThresholdDurationAttributeName + " for command " + END_DEVICE_CONTROL_TYPE)
    public void testCommandArgumentSpecNotFound() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.status = BreakerStatus.armed;
        contactorInfo.loadLimit = contactorInfo.new LoadLimit(BigDecimal.TEN, "kWh");
        contactorInfo.loadTolerance = 30;
        Quantity quantity = Quantity.create(BigDecimal.TEN, 3, "Wh");

        when(commandFactory.createEnableLoadLimitCommand(endDevice, quantity)).thenReturn(endDeviceCommand);
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.emptyList());

        // Business method
        headEndController.performContactorOperations(endDevice, serviceCall, contactorInfo);
    }

    @Test
    @Expected(value = LocalizedException.class, message = "Could not find destination spec with name " + CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION)
    public void testDestinationSpecNotFound() throws Exception {
        ContactorInfo contactorInfo = new ContactorInfo();
        contactorInfo.status = BreakerStatus.armed;

        when(messageService.getDestinationSpec(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION)).thenReturn(Optional.empty());

        // Business method
        headEndController.performContactorOperations(endDevice, serviceCall, contactorInfo);
    }
}