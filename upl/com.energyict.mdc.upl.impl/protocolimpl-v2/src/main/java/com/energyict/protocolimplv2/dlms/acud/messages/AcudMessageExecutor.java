package com.energyict.protocolimplv2.dlms.acud.messages;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ChargeSetup;
import com.energyict.dlms.cosem.CreditSetup;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.dlms.cosem.attributes.ChargeSetupAttributes;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.methods.ChargeSetupMethods;
import com.energyict.dlms.cosem.methods.CreditSetupMethods;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.TempFileLoader;
import com.energyict.protocolimplv2.common.DisconnectControlState;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.acud.AcudCreditUtils;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ChargeDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.CreditDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.energyict.cbo.BaseUnit.COUNT;
import static com.energyict.cbo.BaseUnit.UNITLESS;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysDayIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysFormatDatesAttributeName;

public class AcudMessageExecutor extends AbstractMessageExecutor {

    public static final ObisCode CREDIT_DAYS_LIMIT = ObisCode.fromString("0.0.94.20.70.255");
    public static final ObisCode MONEY_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.67.255");
    public static final ObisCode CONSUMPTION_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.68.255");
    public static final ObisCode TIME_CREDIT_THRESHOLD = ObisCode.fromString("0.0.94.20.69.255");
    public static final ObisCode SCRIPTS_OBIS = ObisCode.fromString("0.0.10.0.2.255");

    private static final ObisCode CHARGE_MODE_SCHEDULER_OBIS = ObisCode.fromString("0.0.15.0.8.255");
    private static final ObisCode CHARGE_TOU_IMPORT = ObisCode.fromString("0.0.19.20.0.255");
    private static final ObisCode CHARGE_CONSUMPTION_TAX = ObisCode.fromString("0.0.94.20.58.255");
    private static final ObisCode CHARGE_MONTHLY_TAX = ObisCode.fromString("0.0.19.20.2.255");
    public static final ObisCode PASSIVE_STEP_TARIFF_OBIS = ObisCode.fromString("0.0.94.20.75.255");
    public static final ObisCode PASSIVE_TAX_RATES_OBIS = ObisCode.fromString("0.0.94.20.77.255");
    public static final ObisCode STEP_TARIFF_SCHEDULER_OBIS = ObisCode.fromString("0.0.15.0.9.255");
    public static final ObisCode TAX_SCHEDULER_OBIS = ObisCode.fromString("0.0.15.0.10.255");

    public static final ObisCode FRIENDLY_DAY_PERIOD_OBIS = ObisCode.fromString("0.0.94.20.72.255");
    public static final ObisCode FRIENDLY_WEEKDAYS_OBIS = ObisCode.fromString("0.0.94.20.73.255");
    public static final ObisCode BREAKER_STATUS = ObisCode.fromString("0.0.96.3.10.255");

    private static final int COMMODITY_OBIS_CLASS_ID = 3;
    private static final ObisCode COMMODITY_OBIS_CODE = ObisCode.fromString("7.0.3.1.0.255");
    private static final int COMMODITY_OBIS_ATTRIBUT_INDEX = 3;

    private static final int STEP_TARIFF_SWITCH = 3;
    public static final int TIME_LENGTH_IN_SEC = 4;
    public static final String CHARGE_TABLE_TIME = "chargeTableTime";
    public static final String CHARGE_TABLE_UNIT = "chargeTableUnit";

    public static final String CHARGE_STEP = "chargeStep";
    public static final String PRICE_STEP = "priceStep";
    public static final String RECALCULATION_TYPE_STEP = "recalculationTypeStep";
    public static final String GRACE_WARNING_STEP = "graceWarningStep";
    public static final String ADDITIONAL_TAX_STEP = "additionalTaxStep";

    public AcudMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            try {
                collectedMessage = executeMessage(pendingMessage, collectedMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER)) {
            upgradeFirmware(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(CreditDeviceMessage.UPDATE_MONEY_CREDIT_THRESHOLD)) {
            updateMoneyCreditThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(CreditDeviceMessage.UPDATE_CONSUMPTION_CREDIT_THRESHOLD)) {
            updateConsumptionCreditThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(CreditDeviceMessage.UPDATE_TIME_CREDIT_THRESHOLD)) {
            updateTimeCreditThreshold(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(CreditDeviceMessage.UPDATE_CREDIT_AMOUNT)) {
            collectedMessage = updateCreditAmount(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(CreditDeviceMessage.UPDATE_CREDIT_DAYS_LIMIT)) {
            updateCreditDaysLimit(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.ACTIVATE_PASSIVE_UNIT_CHARGE)) {
            activatePassiveUnitCharge(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION)) {
            changePassiveUnitChargeWithActivation(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION_DATE)) {
            changePassiveUnitChargeWithActivationDate(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.UPDATE_UNIT_CHARGE)) {
            updateUnitCharge(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.SWITCH_CHARGE_MODE)) {
            switchChargeMode(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_CHARGE_PERIOD)) {
            changeChargePeriod(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_CHARGE_PROPORTION)) {
            changeChargeProportion(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.SWITCH_TAX_AND_STEP_TARIFF)) {
            switchTaxAndStepTariff(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_STEP_TARIFF)) {
            changeStepTariffConfig(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_TAX_RATES)) {
            changeTaxRates(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.FRIENDLY_DAY_PERIOD_UPDATE)) {
            friendlyPeriodUpdate(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.FRIENDLY_WEEKDAYS_UPDATE)) {
            friendlyWeekdaysUpdate(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND)) {
            writeSpecialDays(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SPECIAL_DAY_CSV_STRING)) {
            writeSpecialDaysCsv(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN)) {
            collectedMessage = remoteDisconnect(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE)) {
            collectedMessage = remoteReconnect(pendingMessage);
        } else {   //Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    private ObisCode getCreditTypeObiscode(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        String description = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.creditType);
        return AcudCreditUtils.getCreditTypeObiscode(CreditDeviceMessage.CreditType.entryForDescription(description));
    }

    private ObisCode getChargeTypeObiscode(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        String description = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.chargeType);
        int chargeNo = ChargeDeviceMessage.ChargeType.entryForDescription(description).getId();
        switch (chargeNo) {
            case 1:
                return CHARGE_TOU_IMPORT;
            case 2:
                return CHARGE_MONTHLY_TAX;
            default:
                return CHARGE_CONSUMPTION_TAX;
        }
    }

    protected void updateMoneyCreditThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer remainingCreditHigh = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingCreditHigh));
        Integer remainingCreditLow = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingCreditLow));
        Structure thresholdStructure = new Structure();
        thresholdStructure.addDataType(new Integer32(remainingCreditHigh));
        thresholdStructure.addDataType(new Integer32(remainingCreditLow));
        getCosemObjectFactory().writeObject(MONEY_CREDIT_THRESHOLD, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), thresholdStructure.getBEREncodedByteArray());
    }

    protected void updateConsumptionCreditThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer consumedCreditHigh = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumedCreditHigh));
        Integer consumedCreditLow = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumedCreditLow));
        Structure thresholdStructure = new Structure();
        thresholdStructure.addDataType(new Unsigned16(consumedCreditHigh));
        thresholdStructure.addDataType(new Unsigned16(consumedCreditLow));
        getCosemObjectFactory().writeObject(CONSUMPTION_CREDIT_THRESHOLD, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), thresholdStructure.getBEREncodedByteArray());
    }

    protected void updateTimeCreditThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer remainingTimeHigh = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingTimeHigh));
        Integer remainingTimeLow = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.remainingTimeLow));
        Structure thresholdStructure = new Structure();
        thresholdStructure.addDataType(new Unsigned8(remainingTimeHigh));
        thresholdStructure.addDataType(new Unsigned8(remainingTimeLow));
        getCosemObjectFactory().writeObject(TIME_CREDIT_THRESHOLD, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), thresholdStructure.getBEREncodedByteArray());
    }

    private CollectedMessage updateCreditAmount(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode chargeObisCode = getCreditTypeObiscode(pendingMessage);
        CreditSetup creditSetup = getCosemObjectFactory().getCreditSetup(chargeObisCode);
        Integer creditAmount = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.creditAmount));
        creditSetup.invokeCreditMethod(CreditSetupMethods.UPDATE_AMOUNT, new Integer32(creditAmount));

        int creditType = CreditDeviceMessage.CreditType.entryForDescription(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.creditType)).getId();
        CollectedCreditAmount cca = getProtocol().getCreditAmounts().stream()
                .filter(creditAmount1 -> creditType == CreditDeviceMessage.CreditType.entryForDescription(creditAmount1.getCreditType()).getId() )
                .findAny()
                .orElse(null);

        if( cca != null ) {
            Register register = new Register(-1, AcudCreditUtils.getCreditTypeObiscode(CreditDeviceMessage.CreditType.entryForDescription(cca.getCreditType())), pendingMessage.getDeviceSerialNumber());
            List<CollectedRegister> collectedRegisters = new ArrayList<>();
            final RegisterValue registerValue = new RegisterValue(register, new Quantity(cca.getCreditAmount().get(), Unit.get(COUNT, 0)));
            collectedRegisters.add(createCollectedRegister(registerValue, pendingMessage));

            CollectedMessage collectedMessage = createCollectedMessageWithRegisterData(pendingMessage, collectedRegisters);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

            return collectedMessage;
        }
        return null;
    }

    private void updateCreditDaysLimit(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer creditDaysLimitFirst = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.creditDaysLimitFirst));
        Integer creditDaysLimitScnd = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.creditDaysLimitScnd));
        Structure creditDaysLimit = new Structure();
        creditDaysLimit.addDataType(new Unsigned16(creditDaysLimitFirst));
        creditDaysLimit.addDataType(new Unsigned16(creditDaysLimitScnd));
        getCosemObjectFactory().writeObject(CREDIT_DAYS_LIMIT, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), creditDaysLimit.getBEREncodedByteArray());
    }

    private void activatePassiveUnitCharge(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode chargeObisCode = getChargeTypeObiscode(pendingMessage);
        getCosemObjectFactory().getChargeSetup(chargeObisCode).invokeChargeMethod(ChargeSetupMethods.ACTIVATE_PASSIVE_UNIT_CHARGE);
    }

    private void changePassiveUnitChargeWithActivation(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode chargeObisCode = getChargeTypeObiscode(pendingMessage);
        ChargeSetup chargeSetup = getCosemObjectFactory().getChargeSetup(chargeObisCode);
        Boolean passiveImmediateActivation = Boolean.parseBoolean(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.passiveImmediateActivation));
        Structure passiveUnitChargeStructure = createPassiveUnitCharge(pendingMessage);
        chargeSetup.writeChargeAttribute(ChargeSetupAttributes.UNIT_CHARGE_PASSIVE, passiveUnitChargeStructure);
        if (passiveImmediateActivation)
            chargeSetup.invokeChargeMethod(ChargeSetupMethods.ACTIVATE_PASSIVE_UNIT_CHARGE);
    }

    private void changePassiveUnitChargeWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode chargeObisCode = getChargeTypeObiscode(pendingMessage);
        ChargeSetup chargeSetup = getCosemObjectFactory().getChargeSetup(chargeObisCode);
        OctetString activationTimeInSec = attributeTimeInSec(pendingMessage, DeviceMessageConstants.passiveUnitChargeActivationTime);
        chargeSetup.writeChargeAttribute(ChargeSetupAttributes.UNIT_CHARGE_ACTIVATION_TIME, activationTimeInSec);
        Structure passiveUnitChargeStructure = createPassiveUnitCharge(pendingMessage);
        chargeSetup.writeChargeAttribute(ChargeSetupAttributes.UNIT_CHARGE_PASSIVE, passiveUnitChargeStructure);
    }

    private OctetString attributeTimeInSec(OfflineDeviceMessage pendingMessage, String attributeName) {
        String attributeTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, attributeName).getValue();
        Calendar activationTime = Calendar.getInstance(getProtocol().getTimeZone());
        activationTime.setTimeInMillis(Long.valueOf(attributeTime));
        return new OctetString(ProtocolTools.getBytesFromLong(activationTime.getTime().getTime() / 1000, TIME_LENGTH_IN_SEC));
    }

    private Structure createPassiveUnitCharge(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        Structure passiveUnitChargeStructure = new Structure();
        Structure passiveUnitChargeElement0 = createPassiveUnitChargeElement0(pendingMessage);
        passiveUnitChargeStructure.addDataType(passiveUnitChargeElement0);
        Structure passiveUnitChargeElement1 = createPassiveUnitChargeElement1();
        passiveUnitChargeStructure.addDataType(passiveUnitChargeElement1);
        createPassiveUnitChargeElement2(pendingMessage);
        return passiveUnitChargeStructure;
    }

    private Structure createPassiveUnitChargeElement0(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        Structure passiveUnitChargeElement0 = new Structure();
        Integer chargeCommodityScale = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.chargeCommodityScale));
        passiveUnitChargeElement0.addDataType(new Integer8(chargeCommodityScale));
        Integer chargePriceScale = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.chargePriceScale));
        passiveUnitChargeElement0.addDataType(new Integer8(chargePriceScale));
        return passiveUnitChargeElement0;
    }

    private Structure createPassiveUnitChargeElement1() {
        Structure passiveUnitChargeElement1 = new Structure();
        passiveUnitChargeElement1.addDataType(new Unsigned16(COMMODITY_OBIS_CLASS_ID));
        passiveUnitChargeElement1.addDataType(OctetString.fromObisCode(COMMODITY_OBIS_CODE));
        passiveUnitChargeElement1.addDataType(new Integer8(COMMODITY_OBIS_ATTRIBUT_INDEX));
        return passiveUnitChargeElement1;
    }

    private void createPassiveUnitChargeElement2(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        Structure passiveUnitChargeElement2 = new Structure();
        Array passiveUnitChargeTable = new Array();
        for (int i = 1; i <= 10; i++) {
            Structure chargeTableElement = new Structure();
            String chargeTableTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, CHARGE_TABLE_TIME + i).getValue();
            chargeTableElement.addDataType(OctetString.fromString(chargeTableTime));
            Integer chargeTableUnit = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, CHARGE_TABLE_UNIT + i));
            chargeTableElement.addDataType(new Integer16(chargeTableUnit));
            passiveUnitChargeTable.addDataType(chargeTableElement);
        }
        passiveUnitChargeElement2.addDataType(passiveUnitChargeTable);
    }

    private void updateUnitCharge(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode chargeObisCode = getChargeTypeObiscode(pendingMessage);
        getCosemObjectFactory().getChargeSetup(chargeObisCode).invokeChargeMethod(ChargeSetupMethods.UPDATE_UNIT_CHARGE);
    }

    private void switchChargeMode(OfflineDeviceMessage pendingMessage) throws IOException {
        String attributeTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.activationDate).getValue();
        Calendar activationTime = Calendar.getInstance(getProtocol().getTimeZone());
        activationTime.setTimeInMillis(Long.valueOf(attributeTime));
        SingleActionSchedule singleActionSchedule = getCosemObjectFactory().getSingleActionSchedule(CHARGE_MODE_SCHEDULER_OBIS);
        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(SCRIPTS_OBIS.getLN()));
        scriptStruct.addDataType(new Unsigned16(getChargeMode(pendingMessage)));
        singleActionSchedule.writeExecutedScript(scriptStruct);
        singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(activationTime));
    }

    private int getChargeMode(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        String description = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.chargeMode);
        ChargeDeviceMessage.ChargeMode chargeMode = ChargeDeviceMessage.ChargeMode.entryForDescription(description);
        chargeMode = chargeMode.equals(ChargeDeviceMessage.ChargeMode.Postpaid_charge) ? ChargeDeviceMessage.ChargeMode.Prepaid_charge : ChargeDeviceMessage.ChargeMode.Postpaid_charge;
        return chargeMode.getId();
    }

    private void changeChargePeriod(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode chargeObisCode = getChargeTypeObiscode(pendingMessage);
        ChargeSetup chargeSetup = getCosemObjectFactory().getChargeSetup(chargeObisCode);
        Integer chargePeriod = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.chargePeriod));
        chargeSetup.writeChargeAttribute(ChargeSetupAttributes.PERIOD, new Unsigned32(chargePeriod));
    }

    private void changeChargeProportion(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode chargeObisCode = getChargeTypeObiscode(pendingMessage);
        ChargeSetup chargeSetup = getCosemObjectFactory().getChargeSetup(chargeObisCode);
        Integer chargeProportion = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.chargeProportion));
        chargeSetup.writeChargeAttribute(ChargeSetupAttributes.PROPORTION, new Unsigned16(chargeProportion));
    }

    private void switchTaxAndStepTariff(OfflineDeviceMessage pendingMessage) throws IOException {
        String attributeTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.activationDate).getValue();
        Calendar activationTime = Calendar.getInstance(getProtocol().getTimeZone());
        activationTime.setTimeInMillis(Long.valueOf(attributeTime));
        SingleActionSchedule singleActionSchedule = getCosemObjectFactory().getSingleActionSchedule(STEP_TARIFF_SCHEDULER_OBIS);
        Structure tariffStruct = new Structure();
        tariffStruct.addDataType(new OctetString(SCRIPTS_OBIS.getLN()));
        int tariffType = getTariffType(pendingMessage);
        tariffStruct.addDataType(new Unsigned16(tariffType));
        singleActionSchedule.writeExecutedScript(tariffStruct);
        singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(activationTime));
        if (tariffType == STEP_TARIFF_SWITCH) {
            singleActionSchedule = getCosemObjectFactory().getSingleActionSchedule(TAX_SCHEDULER_OBIS);
            singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(activationTime));
        }
    }

    private int getTariffType(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        String description = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.tariffType);
        return ChargeDeviceMessage.TariffType.entryForDescription(description).getId();
    }

    private void changeStepTariffConfig(OfflineDeviceMessage pendingMessage) throws IOException {
        Structure changeStepTariff = new Structure();
        Integer tarrifCode = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.tariffCode));
        String additionalTaxes = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.additionalTaxesType);
        Integer additionalTaxesId = ChargeDeviceMessage.AdditionalTaxesType.entryForDescription(additionalTaxes).getId();
        String graceRecalculation = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.graceRecalculationType);
        Integer graceRecalculationId = ChargeDeviceMessage.GraceRecalculationType.entryForDescription(graceRecalculation).getId();
        Integer graceRecalculationValue = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.graceRecalculationValue));
        changeStepTariff.addDataType(new Unsigned16(tarrifCode));
        changeStepTariff.addDataType(new TypeEnum(additionalTaxesId));
        changeStepTariff.addDataType(new TypeEnum(graceRecalculationId));
        changeStepTariff.addDataType(new Unsigned16(graceRecalculationValue));
        Array changeStepTariffArray = new Array();
        for (int i = 1; i <= 10; i++) {
            Integer price = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, PRICE_STEP + i));
            String recalculation = getDeviceMessageAttributeValue(pendingMessage, RECALCULATION_TYPE_STEP + i);
            Integer recalculationId = ChargeDeviceMessage.RecalculationType.entryForDescription(recalculation).getId();
            Integer graceWarning = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, GRACE_WARNING_STEP + i));
            Integer additionalTax = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, ADDITIONAL_TAX_STEP + i));
            Structure changeStep = new Structure();
            addStepTarifCharge(pendingMessage, changeStep, i);
            changeStep.addDataType(new Unsigned32(price));
            changeStep.addDataType(new TypeEnum(recalculationId));
            changeStep.addDataType(new Unsigned16(graceWarning));
            changeStep.addDataType(new Unsigned32(additionalTax));
            changeStepTariffArray.addDataType(changeStep);
        }
        changeStepTariff.addDataType(changeStepTariffArray);
        getCosemObjectFactory().writeObject(PASSIVE_STEP_TARIFF_OBIS, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), changeStepTariff.getBEREncodedByteArray());
    }

    private void changeTaxRates(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer monthlyTax = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.monthlyTax));
        Integer zeroConsumptionTax = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.zeroConsumptionTax));
        Integer consumptionTax = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumptionTax));
        Integer consumptionAmount = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumptionAmount));
        Integer consumptionLimit = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.consumptionLimit));
        Structure taxRatesStructure = new Structure();
        taxRatesStructure.addDataType(new Unsigned32(monthlyTax));
        taxRatesStructure.addDataType(new Unsigned32(zeroConsumptionTax));
        taxRatesStructure.addDataType(new Unsigned32(consumptionTax));
        taxRatesStructure.addDataType(new Unsigned16(consumptionAmount));
        taxRatesStructure.addDataType(new Unsigned16(consumptionLimit));
        getCosemObjectFactory().writeObject(PASSIVE_TAX_RATES_OBIS, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), taxRatesStructure.getBEREncodedByteArray());
    }

    protected void addStepTarifCharge(OfflineDeviceMessage pendingMessage, Structure changeStep, Integer step) throws IOException {
        Integer charge = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, CHARGE_STEP + step));
        changeStep.addDataType(new Unsigned16(charge));
    }

    private void friendlyPeriodUpdate(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer hourStart = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.friendlyHourStart));
        Integer minStart = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.friendlyMinuteStart));
        Integer secStart = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.friendlySecondStart));
        Integer hndStart = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.friendlyHundredthsStart));

        Integer hourStop = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.friendlyHourStop));
        Integer minStop = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.friendlyMinuteStop));
        Integer secStop = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.friendlySecondStop));
        Integer hndStop = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.friendlyHundredthsStop));

        Structure startStructure = createTimeStructure(hourStart, minStart, secStart, hndStart);
        Structure stopStructure = createTimeStructure(hourStop, minStop, secStop, hndStop);
        Structure periodStructure = new Structure();
        periodStructure.addDataType(startStructure);
        periodStructure.addDataType(stopStructure);
        getCosemObjectFactory().writeObject(FRIENDLY_DAY_PERIOD_OBIS, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), periodStructure.getBEREncodedByteArray());
    }

    private Structure createTimeStructure(Integer hourStart, Integer minStart, Integer secStart, Integer hndStart) {
        Structure startStructure = new Structure();
        startStructure.addDataType(new Unsigned8(hourStart));
        startStructure.addDataType(new Unsigned8(minStart));
        startStructure.addDataType(new Unsigned8(secStart));
        startStructure.addDataType(new Unsigned8(hndStart));
        return startStructure;
    }

    private void friendlyWeekdaysUpdate(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer weekdays = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.friendlyWeekdays),2);
        BitString weekdaysBits = new BitString(weekdays, 8);
        getCosemObjectFactory().writeObject(FRIENDLY_WEEKDAYS_OBIS, DLMSClassId.DATA.getClassId(), DataAttributes.VALUE.getAttributeNumber(), weekdaysBits.getBEREncodedByteArray());
    }

    private void writeSpecialDays(OfflineDeviceMessage pendingMessage) throws IOException {
        SpecialDaysTable specialDaysTable = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
        String specialDaysHex = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        Array specialDaysArray = new Array(ProtocolTools.getBytesFromHexString(specialDaysHex, ""), 0, 0);
        specialDaysTable.writeSpecialDays(specialDaysArray);
    }

    private void writeSpecialDaysCsv(OfflineDeviceMessage pendingMessage) throws IOException {
        final String dayId = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, specialDaysDayIdAttributeName).getValue();
        final String rawInput = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, specialDaysFormatDatesAttributeName).getValue();
        SpecialDaysTable specialDaysTable = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());

        final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
        int dayIndex = 1;
        Array specialDaysArray = new Array();

        final List<String> specialDaysList = Arrays.asList(rawInput.split("\\n"));

        for (final String specialDay : specialDaysList) {
            try {
                Date date = dateFormat.parse(specialDay);
                Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
                cal.setTime(date);

                final int day = cal.get(Calendar.DAY_OF_MONTH);
                final int month = cal.get(Calendar.MONTH) + 1; // Java month starts from 0
                final int year = cal.get(Calendar.YEAR);

                byte[] timeStampBytes = {
                        (byte) ((year >> 8) & 0xFF),
                        (byte) (year & 0xFF),
                        (byte) (month),
                        (byte) (day),
                        (byte) 0xFF
                };
                OctetString timeStamp = OctetString.fromByteArray(timeStampBytes, timeStampBytes.length);
                Unsigned8 dayType = new Unsigned8(Integer.parseInt(dayId));
                Structure specialDayStructure = new Structure();
                specialDayStructure.addDataType(new Unsigned16(dayIndex));
                specialDayStructure.addDataType(timeStamp);
                specialDayStructure.addDataType(dayType);
                specialDaysArray.addDataType(specialDayStructure);
                dayIndex++;

            } catch (ParseException e) {
                throw new ProtocolException("Invalid format for date given: " + e.getMessage() + ", expected day/month/year.");
            }
        }

        specialDaysTable.writeSpecialDays(specialDaysArray);
    }

    private CollectedMessage remoteDisconnect(OfflineDeviceMessage pendingMessage) throws IOException {
        getProtocol().getDlmsSession().getCosemObjectFactory().getDisconnector().remoteDisconnect();
        return getRemoteBreakerStatus(pendingMessage);
    }

    private CollectedMessage remoteReconnect(OfflineDeviceMessage pendingMessage) throws IOException {
        getProtocol().getDlmsSession().getCosemObjectFactory().getDisconnector().remoteReconnect();
        return getRemoteBreakerStatus(pendingMessage);
    }

    private CollectedMessage getRemoteBreakerStatus(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedBreakerStatus colBreakStatus = getProtocol().getBreakerStatus();
        Register register = new Register(-1, BREAKER_STATUS, pendingMessage.getDeviceSerialNumber());
        List<CollectedRegister> collectedRegisters = new ArrayList<>();
        // Set value of register as text
        Optional<BreakerStatus> breakerStatus = colBreakStatus.getBreakerStatus();
        if (breakerStatus.isPresent()) {
            String registerValueText = breakerStatus.get().getDescription();
            final RegisterValue registerValue = new RegisterValue(register, null, null, null, null, new Date(), 0, registerValueText);
            collectedRegisters.add(createTextCollectedRegister(registerValue, pendingMessage));

            CollectedMessage collectedMessage = createCollectedMessageWithRegisterData(pendingMessage, collectedRegisters);
            return collectedMessage;
        }
        throw new ProtocolException(String.format("Breaker status for device %s not found", pendingMessage.getDeviceSerialNumber()));
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        String path = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.firmwareUpdateFileAttributeName);
        byte[] binaryImage = TempFileLoader.loadTempFile(path);
        // Will return empty string if the MessageAttribute could not be found
        String id = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName);
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();
        imageTransfer.setVerifyImage(true);
        imageTransfer.setActivateImage(true);
        //Use polling to check the result of the image verification
        imageTransfer.setUsePollingVerifyAndActivate(true);
        imageTransfer.upgrade(binaryImage, false, id, false);
    }
}