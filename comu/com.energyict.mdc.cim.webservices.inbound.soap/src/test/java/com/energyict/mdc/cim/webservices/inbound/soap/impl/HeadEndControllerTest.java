/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ami.ChangeTaxRatesInfo;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.FriendlyDayPeriodInfo;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class HeadEndControllerTest {
    public enum TranslationKeys implements TranslationKey {
        CONTACTOR_ACTIVATION_DATE("ContactorDeviceMessage.activationdate", "Activation date"),
        CREDIT_AMOUNT("creditAmount", "Credit Amount"),
        CREDIT_DAYS_LIMIT_FIRST("creditDaysLimitFirst", "Grace period before warning (days)"),
        CREDIT_DAYS_LIMIT_SCND("creditDaysLimitScnd", "Grace period before relay shall be opened (days)"),
        TARIFF_TYPE("tariffType", "Tariff to switch"),
        CHARGE_MODE("chargeMode", "Choose charge mode"),
        ACTIVATION_DATE("activationDate", "Activation date"),
        CREDIT_TYPE("creditType", "Credit Type"),
        MONTHLY_TAX("monthlyTax", "MonthlyTax"),
        ZERO_CONSUMPTION_TAX("zeroConsumptionTax", "Zero Consumption Tax"),
        CONSUMPTION_TAX("consumptionTax", "Consumption Tax"),
        CONSUMPTION_AMOUNT("consumptionAmount", "Consumption Amount (KWH)"),
        CONSUMPTION_LIMIT("consumptionLimit", "Consumption Limit (KWH)"),
        FRIENDLY_HOUR_START("friendlyHourStart", "Friendly Hour Start"),
        FRIENDLY_MINUTE_START("friendlyMinuteStart", "Friendly Minute Start"),
        FRIENDLY_SECOND_START("friendlySecondStart", "Friendly Second Start"),
        FRIENDLY_HUNDREDTHS_START("friendlySecondHundredthsStart", "Friendly Second Hundredths Start"),
        FRIENDLY_HOUR_STOP("friendlyHourStop", "Friendly Hour Stop"),
        FRIENDLY_MINUTE_STOP("friendlyMinuteStop", "Friendly Minute Stop"),
        FRIENDLY_SECOND_STOP("friendlySecondStop", "Friendly Second Stop"),
        FRIENDLY_HUNDREDTHS_STOP("friendlySecondHundredthsStop", "Friendly Second Hundredths Stop"),
        FRIENDLY_WEEKDAYS("friendlyWeekdays", "Friendly Week Days (SuSaFrThWeTuMo)")
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
    private static final String CHANGE_TAX_RATES = "3.20.86.13";
    private static final String SWITCH_TAX_AND_STEP_TARIFF = "3.20.283.54";
    private static final String SWITCH_CHARGE_MODE = "3.20.9.13";
    private static final String FRIENDLY_DAY_PERIOD_UPDATE = "0.20.114.13";
    private static final String FRIENDLY_WEEKDAYS_UPDATE = "0.20.35.13";
    private static final String SPECIAL_DAY_CALENDAR_SEND = "0.20.97.13";

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

        assertThatThrownBy(() -> headEndController.performOperations(endDevice, serviceCall, new DeviceCommandInfo(), NOW_DATE, false))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("Couldn't find the head-end interface for end device with MRID 'endDeviceMRID'.");
    }

    @Test
    public void testCloseRemoteSwitchOperation() throws Exception {
        mockEndDeviceControlType(CLOSE_REMOTE_SWITCH);

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(CLOSE_REMOTE_SWITCH, Collections.emptyList());

        when(commandFactory.createConnectCommand(endDevice, null)).thenReturn(endDeviceCommand);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        verify(commandFactory).createConnectCommand(endDevice, null);
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
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
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        verify(commandFactory).createConnectCommand(endDevice, NOW_DATE);
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
    }

    @Test
    public void testOpenRemoteSwitchOperation() throws Exception {
        mockEndDeviceControlType(OPEN_REMOTE_SWITCH);

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(OPEN_REMOTE_SWITCH, Collections.emptyList());

        when(commandFactory.createDisconnectCommand(endDevice, null)).thenReturn(endDeviceCommand);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        verify(commandFactory).createDisconnectCommand(endDevice, null);
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
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
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        verify(commandFactory).createDisconnectCommand(endDevice, NOW_DATE);
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
    }


    @Test
    public void testChangeTaxRatesOperation() throws Exception {
        mockEndDeviceControlType(CHANGE_TAX_RATES);


        List<EndDeviceControlAttribute> attributes = new ArrayList<>(Arrays.asList(
                createAttribute(TranslationKeys.MONTHLY_TAX, "1"),
                createAttribute(TranslationKeys.ZERO_CONSUMPTION_TAX, "1"),
                createAttribute(TranslationKeys.CONSUMPTION_TAX, "1"),
                createAttribute(TranslationKeys.CONSUMPTION_AMOUNT, "1"),
                createAttribute(TranslationKeys.CONSUMPTION_LIMIT, "1")));

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(CHANGE_TAX_RATES, attributes);

        when(commandFactory.createChangeTaxRatesCommand(eq(endDevice), any(ChangeTaxRatesInfo.class))).thenReturn(endDeviceCommand);
        List<PropertySpec> propertySpecs = new ArrayList<>();
        PropertySpec monthlyTaxSpec = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.MONTHLY_TAX)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec zeroConsumptionTaxSpec = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.ZERO_CONSUMPTION_TAX)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec consumptionTaxSpec = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.CONSUMPTION_TAX)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec consumptionLimitSpec = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.CONSUMPTION_LIMIT)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec consumptionAmountSpec = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.CONSUMPTION_AMOUNT)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        propertySpecs.add(monthlyTaxSpec);
        propertySpecs.add(zeroConsumptionTaxSpec);
        propertySpecs.add(consumptionTaxSpec);
        propertySpecs.add(consumptionLimitSpec);
        propertySpecs.add(consumptionAmountSpec);

        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(propertySpecs);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        ArgumentCaptor<ChangeTaxRatesInfo> argument = ArgumentCaptor.forClass(ChangeTaxRatesInfo.class);
        verify(commandFactory).createChangeTaxRatesCommand(eq(endDevice), argument.capture());
        assert (argument.getValue().monthlyTax.equals(BigDecimal.ONE));
        assert (argument.getValue().zeroConsumptionTax.equals(BigDecimal.ONE));
        assert (argument.getValue().consumptionTax.equals(BigDecimal.ONE));
        assert (argument.getValue().consumptionAmount.equals(BigDecimal.ONE));
        assert (argument.getValue().consumptionLimit.equals(BigDecimal.ONE));

        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
    }

    @Test(expected = CommandException.class)
    public void testSwitchTaxAndStepTariffOperationTariffTypeAttributeMissing() throws Exception {
        mockEndDeviceControlType(SWITCH_TAX_AND_STEP_TARIFF);
        List<EndDeviceControlAttribute> attributes = new ArrayList<>(Collections.singletonList(createAttribute(TranslationKeys.ACTIVATION_DATE, NOW_DATE.toString())));

        headEndController.checkOperation(SWITCH_TAX_AND_STEP_TARIFF, attributes);
    }

    @Test(expected = CommandException.class)
    public void testSwitchTaxAndStepTariffOperationActivationDateAttributeMissing() throws Exception {
        mockEndDeviceControlType(SWITCH_TAX_AND_STEP_TARIFF);
        List<EndDeviceControlAttribute> attributes = new ArrayList<>(Collections.singletonList(createAttribute(TranslationKeys.TARIFF_TYPE, "Some")));

        headEndController.checkOperation(SWITCH_TAX_AND_STEP_TARIFF, attributes);
    }

    @Test
    public void testSwitchTaxAndStepTariffOperation() throws Exception {
        mockEndDeviceControlType(SWITCH_TAX_AND_STEP_TARIFF);
        List<EndDeviceControlAttribute> attributes = new ArrayList<>(Arrays.asList(
                createAttribute(TranslationKeys.TARIFF_TYPE, "Passive step tariff"),
                createAttribute(TranslationKeys.ACTIVATION_DATE, NOW_DATE.toString())));

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(SWITCH_TAX_AND_STEP_TARIFF, attributes);

        when(commandFactory.createSwitchTaxAndStepTariffCommand(endDevice, "Passive step tariff", NOW_DATE)).thenReturn(endDeviceCommand);
        List<PropertySpec> propertySpecs = new ArrayList<>();
        PropertySpec tariffTypeSpec = propertySpecService
                .specForValuesOf(new StringFactory())
                .named(TranslationKeys.TARIFF_TYPE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec dateTimeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(TranslationKeys.ACTIVATION_DATE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        propertySpecs.add(tariffTypeSpec);
        propertySpecs.add(dateTimeSpec);
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(propertySpecs);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        verify(commandFactory).createSwitchTaxAndStepTariffCommand(endDevice, "Passive step tariff", NOW_DATE);
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
    }

    @Test(expected = CommandException.class)
    public void testSwitchSwitchChargeModeOperationTariffTypeAttributeMissing() throws Exception {
        mockEndDeviceControlType(SWITCH_CHARGE_MODE);
        List<EndDeviceControlAttribute> attributes = new ArrayList<>(Collections.singletonList(createAttribute(TranslationKeys.ACTIVATION_DATE, NOW_DATE.toString())));

        headEndController.checkOperation(SWITCH_CHARGE_MODE, attributes);
    }

    @Test(expected = CommandException.class)
    public void testSwitchSwitchChargeModeOperationActivationDateAttributeMissing() throws Exception {
        mockEndDeviceControlType(SWITCH_CHARGE_MODE);
        List<EndDeviceControlAttribute> attributes = new ArrayList<>(Collections.singletonList(createAttribute(TranslationKeys.CHARGE_MODE, "Some")));

        headEndController.checkOperation(SWITCH_CHARGE_MODE, attributes);
    }

    @Test
    public void testSwitchChargeModeOperation() throws Exception {
        mockEndDeviceControlType(SWITCH_CHARGE_MODE);
        List<EndDeviceControlAttribute> attributes = new ArrayList<>(Arrays.asList(
                createAttribute(TranslationKeys.CHARGE_MODE, "Prepaid"),
                createAttribute(TranslationKeys.ACTIVATION_DATE, NOW_DATE.toString())));

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(SWITCH_CHARGE_MODE, attributes);

        when(commandFactory.createSwitchChargeModeCommand(endDevice, "Prepaid", NOW_DATE)).thenReturn(endDeviceCommand);
        List<PropertySpec> propertySpecs = new ArrayList<>();
        PropertySpec chargeTypeSpec = propertySpecService
                .specForValuesOf(new StringFactory())
                .named(TranslationKeys.CHARGE_MODE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec dateTimeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(TranslationKeys.ACTIVATION_DATE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        propertySpecs.add(chargeTypeSpec);
        propertySpecs.add(dateTimeSpec);
        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(propertySpecs);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        verify(commandFactory).createSwitchChargeModeCommand(endDevice, "Prepaid", NOW_DATE);
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
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
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        verify(commandFactory).createUpdateCreditAmountCommand(endDevice, "Import Credit", BigDecimal.valueOf(1));
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
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
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        verify(commandFactory).createUpdateCreditDaysLimitCommand(endDevice, BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
    }

    @Test
    public void testUpdateFriendlyDayPeriodOperation() throws Exception {
        mockEndDeviceControlType(FRIENDLY_DAY_PERIOD_UPDATE);


        List<EndDeviceControlAttribute> attributes = new ArrayList<>(Arrays.asList(
                createAttribute(TranslationKeys.FRIENDLY_HOUR_START, "1"),
                createAttribute(TranslationKeys.FRIENDLY_MINUTE_START, "1"),
                createAttribute(TranslationKeys.FRIENDLY_SECOND_START, "1"),
                createAttribute(TranslationKeys.FRIENDLY_HUNDREDTHS_START, "1"),
                createAttribute(TranslationKeys.FRIENDLY_HOUR_STOP, "10"),
                createAttribute(TranslationKeys.FRIENDLY_MINUTE_STOP, "10"),
                createAttribute(TranslationKeys.FRIENDLY_SECOND_STOP, "10"),
                createAttribute(TranslationKeys.FRIENDLY_HUNDREDTHS_STOP, "10")));

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(FRIENDLY_DAY_PERIOD_UPDATE, attributes);

        when(commandFactory.createUpdateFriendlyDayPeriodCommand(eq(endDevice), any(FriendlyDayPeriodInfo.class))).thenReturn(endDeviceCommand);
        List<PropertySpec> propertySpecs = new ArrayList<>();
        PropertySpec friendlyHourStart = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.FRIENDLY_HOUR_START)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec friendlyMinuteStart = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.FRIENDLY_MINUTE_START)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec friendlySecondStart = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.FRIENDLY_SECOND_START)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec friendlyHundredthsStart = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.FRIENDLY_HUNDREDTHS_START)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec friendlyHourStop = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.FRIENDLY_HOUR_STOP)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec friendlyMinuteStop = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.FRIENDLY_MINUTE_STOP)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec friendlySecondStop = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.FRIENDLY_SECOND_STOP)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec friendlyHundredthsStop = propertySpecService
                .specForValuesOf(new BigDecimalFactory())
                .named(TranslationKeys.FRIENDLY_HUNDREDTHS_STOP)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        propertySpecs.add(friendlyHourStart);
        propertySpecs.add(friendlyMinuteStart);
        propertySpecs.add(friendlySecondStart);
        propertySpecs.add(friendlyHundredthsStart);
        propertySpecs.add(friendlyHourStop);
        propertySpecs.add(friendlyMinuteStop);
        propertySpecs.add(friendlySecondStop);
        propertySpecs.add(friendlyHundredthsStop);

        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(propertySpecs);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        ArgumentCaptor<FriendlyDayPeriodInfo> argument = ArgumentCaptor.forClass(FriendlyDayPeriodInfo.class);
        verify(commandFactory).createUpdateFriendlyDayPeriodCommand(eq(endDevice), argument.capture());
        assert (argument.getValue().friendlyHourStart.equals(BigDecimal.ONE));
        assert (argument.getValue().friendlyMinuteStart.equals(BigDecimal.ONE));
        assert (argument.getValue().friendlySecondStart.equals(BigDecimal.ONE));
        assert (argument.getValue().friendlySecondHundredthsStart.equals(BigDecimal.ONE));
        assert (argument.getValue().friendlyHourStop.equals(BigDecimal.TEN));
        assert (argument.getValue().friendlyMinuteStop.equals(BigDecimal.TEN));
        assert (argument.getValue().friendlySecondStop.equals(BigDecimal.TEN));
        assert (argument.getValue().friendlySecondHundredthsStop.equals(BigDecimal.TEN));

        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
    }

    @Test
    public void testUpdateFriendlyWeekdaysOperation() throws Exception {
        mockEndDeviceControlType(FRIENDLY_WEEKDAYS_UPDATE);
        List<EndDeviceControlAttribute> attributes = Collections.singletonList(
                createAttribute(TranslationKeys.FRIENDLY_WEEKDAYS, "0000000"));

        DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(FRIENDLY_WEEKDAYS_UPDATE, attributes);

        when(commandFactory.createUpdateFriendlyWeekdaysCommand(endDevice, "0000000")).thenReturn(endDeviceCommand);
        List<PropertySpec> propertySpecs = new ArrayList<>();
        PropertySpec friendlyWeekdays = propertySpecService
                .specForValuesOf(new StringFactory())
                .named(TranslationKeys.FRIENDLY_WEEKDAYS)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();

        propertySpecs.add(friendlyWeekdays);

        when(endDeviceCommand.getCommandArgumentSpecs()).thenReturn(propertySpecs);

        // Business method
        headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, NOW_DATE, false);

        // Asserts
        verify(commandFactory).createUpdateFriendlyWeekdaysCommand(endDevice, "0000000");
        verify(headEndInterface).sendCommand(endDeviceCommand, NOW_DATE, serviceCall, false);
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
