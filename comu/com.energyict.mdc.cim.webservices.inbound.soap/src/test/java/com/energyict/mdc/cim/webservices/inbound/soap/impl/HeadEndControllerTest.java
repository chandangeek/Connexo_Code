/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;

import com.energyict.mdc.dynamic.DateAndTimeFactory;

import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControlAttribute;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HeadEndControllerTest {
    public enum TranslationKeys implements TranslationKey {
        CONTACTOR_ACTIVATION_DATE("ContactorDeviceMessage.activationdate", "Activation date"),
        CREDIT_AMOUNT("creditAmount", "Credit Amount"),
        CREDIT_DAYS_LIMIT_FIRST("creditDaysLimitFirst", "Grace period before warning (days)"),
        CREDIT_DAYS_LIMIT_SCND("creditDaysLimitScnd", "Grace period before relay shall be opened (days)"),
        CREDIT_TYPE("creditType", "Credit Type"),
        ;

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }

    private static final String CLOSE_REMOTE_SWITCH = "6.31.0.18";
    private static final String OPEN_REMOTE_SWITCH = "3.31.0.23";
    private static final String UPDATE_CREDIT_DAYS_LIMIT = "9.20.8.13";
    private static final String UPDATE_CREDIT_AMOUNT = "3.20.22.13";
    private static final String UNSUPPORTED_CODE = "9.9.9.9";
    private static final String UNSUPPORTED_CODE_BY_CIM_HEADEND_CONTROLLER = "0.12.32.13";
    private static final Instant NOW_DATE = ZonedDateTime.of(2020, 6, 24, 9, 5, 0, 0,
            TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final String END_DEVICE_MRID = "endDeviceMRID";

    @Mock
    private EndDevice endDevice;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private HeadEndInterface headEndInterface;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CommandFactory commandFactory;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private BeanService beanService;
    @Mock
    private EndDeviceCommand endDeviceCommand;
    @Mock
    private EndDeviceControlType endDeviceControlType;
    @Mock
    private Clock clock;

    private HeadEndController headEndController;
    private PropertySpecService propertySpecService;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    @Before
    public void setUp() throws Exception {
        headEndController = new HeadEndController(thesaurus, clock);

        when(endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
        when(endDevice.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));
        when(headEndInterface.getCommandFactory()).thenReturn(commandFactory);

        this.propertySpecService = new PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService);
        when(this.clock.instant()).thenReturn(NOW_DATE);
    }

    @Test
    public void testInvalidOperation() throws Exception {
        when(endDeviceControlType.getMRID()).thenReturn(UNSUPPORTED_CODE);

        assertThatThrownBy(() -> headEndController.checkOperation(UNSUPPORTED_CODE, Collections.emptyList()))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("End device control type with CIM code '9.9.9.9' isn't supported.");

        when(endDeviceControlType.getMRID()).thenReturn(UNSUPPORTED_CODE_BY_CIM_HEADEND_CONTROLLER);

        assertThatThrownBy(() -> headEndController.checkOperation(UNSUPPORTED_CODE_BY_CIM_HEADEND_CONTROLLER, Collections.emptyList()))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("End device control type with CIM code '0.12.32.13' isn't supported.");
    }

    @Test
    public void testInvalidDevice() throws Exception {
        when(endDevice.getHeadEndInterface()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> headEndController.performOperations(endDevice, serviceCall, new DeviceCommandInfo(), NOW_DATE))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("Couldn't find the head-end interface for end device with MRID 'endDeviceMRID'.");
    }

    @Test
    public void testCloseRemoteSwitchOperation() throws Exception {
        mockEndDeviceControlType(CLOSE_REMOTE_SWITCH);

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(CLOSE_REMOTE_SWITCH, Collections.emptyList());

        when(commandFactory.createConnectCommand(endDevice, null)).thenReturn(endDeviceCommand);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE);

        // Asserts
        verify(commandFactory).createConnectCommand(endDevice, null);
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall);
    }

    @Test
    public void testCloseRemoteSwitchOperationWithActivationDate() throws Exception {
        mockEndDeviceControlType(CLOSE_REMOTE_SWITCH);
        List<EndDeviceControlAttribute> attributes = new ArrayList<>(Collections.singleton(createAttribute(TranslationKeys.CONTACTOR_ACTIVATION_DATE,
                NOW_DATE.toString())));

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(CLOSE_REMOTE_SWITCH, attributes);

        when(commandFactory.createConnectCommand(endDevice, NOW_DATE)).thenReturn(endDeviceCommand);
        PropertySpec dateTimeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(TranslationKeys.CONTACTOR_ACTIVATION_DATE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.singletonList(dateTimeSpec));

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE);

        // Asserts
        verify(commandFactory).createConnectCommand(endDevice, NOW_DATE);
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall);
    }

    @Test
    public void testOpenRemoteSwitchOperation() throws Exception {
        mockEndDeviceControlType(OPEN_REMOTE_SWITCH);

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(OPEN_REMOTE_SWITCH, Collections.emptyList());

        when(commandFactory.createDisconnectCommand(endDevice, null)).thenReturn(endDeviceCommand);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE);

        // Asserts
        verify(commandFactory).createDisconnectCommand(endDevice, null);
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall);
    }

    @Test
    public void testOpenRemoteSwitchOperationWithActivationDate() throws Exception {
        mockEndDeviceControlType(OPEN_REMOTE_SWITCH);
        List<EndDeviceControlAttribute> attributes = new ArrayList<>(Collections.singleton(createAttribute(TranslationKeys.CONTACTOR_ACTIVATION_DATE,
                NOW_DATE.toString())));

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(OPEN_REMOTE_SWITCH, attributes);

        when(commandFactory.createDisconnectCommand(endDevice, NOW_DATE)).thenReturn(endDeviceCommand);
        PropertySpec dateTimeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(TranslationKeys.CONTACTOR_ACTIVATION_DATE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(Collections.singletonList(dateTimeSpec));

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE);

        // Asserts
        verify(commandFactory).createDisconnectCommand(endDevice, NOW_DATE);
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall);
    }

    @Test
    public void testUpdateCreditAmountOperation() throws Exception {
        mockEndDeviceControlType(UPDATE_CREDIT_AMOUNT);
        List<EndDeviceControlAttribute> attributes = new ArrayList<>();
        attributes.add(createAttribute(TranslationKeys.CREDIT_TYPE, "Import Credit"));
        attributes.add(createAttribute(TranslationKeys.CREDIT_AMOUNT, "1"));

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(UPDATE_CREDIT_AMOUNT, attributes);

        when(commandFactory.createUpdateCreditAmountCommand(endDevice, "Import Credit", BigDecimal.valueOf(1))).thenReturn(endDeviceCommand);
        List<PropertySpec> propertySpecs = new ArrayList<>();
        PropertySpec creditTypeSpec = propertySpecService
                .specForValuesOf(new StringFactory())
                .named(TranslationKeys.CREDIT_TYPE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec creditAmountSpec = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.CREDIT_AMOUNT)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        propertySpecs.add(creditTypeSpec);
        propertySpecs.add(creditAmountSpec);

        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(propertySpecs);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE);

        // Asserts
        verify(commandFactory).createUpdateCreditAmountCommand(endDevice, "Import Credit", BigDecimal.valueOf(1));
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall);
    }

    @Test
    public void testUpdateCreditDaysLimitOperation() throws Exception {
        mockEndDeviceControlType(UPDATE_CREDIT_DAYS_LIMIT);
        List<EndDeviceControlAttribute> attributes = new ArrayList<>();
        attributes.add(createAttribute(TranslationKeys.CREDIT_DAYS_LIMIT_FIRST, "1"));
        attributes.add(createAttribute(TranslationKeys.CREDIT_DAYS_LIMIT_SCND, "2"));

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(UPDATE_CREDIT_DAYS_LIMIT, attributes);

        when(commandFactory.createUpdateCreditDaysLimitCommand(endDevice, BigDecimal.valueOf(1), BigDecimal.valueOf(2))).thenReturn(endDeviceCommand);
        List<PropertySpec> propertySpecs = new ArrayList<>();
        PropertySpec creditTypeSpec = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.CREDIT_DAYS_LIMIT_FIRST)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec creditAmountSpec = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.CREDIT_DAYS_LIMIT_SCND)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        propertySpecs.add(creditTypeSpec);
        propertySpecs.add(creditAmountSpec);

        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(propertySpecs);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE);

        // Asserts
        verify(commandFactory).createUpdateCreditDaysLimitCommand(endDevice, BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall);
    }

    private void mockEndDeviceControlType(String mRID) {
        when(endDeviceControlType.getMRID()).thenReturn(mRID);
        when(endDeviceCommand.getEndDeviceControlType()).thenReturn(endDeviceControlType);
    }

    private EndDeviceControlAttribute createAttribute(TranslationKeys name, String value) {
        EndDeviceControlAttribute attribute = new EndDeviceControlAttribute();
        attribute.setName(name.getKey());
        attribute.setValue(value);
        return attribute;
    }
}
