package com.energyict.protocolimplv2.dlms.acud.messages;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.ChargeSetupAttributes;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.methods.ChargeSetupMethods;
import com.energyict.dlms.cosem.methods.CreditSetupMethods;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.TempFileLoader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

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

    private static final ObisCode IMPORT_CREDIT = ObisCode.fromString("0.0.19.10.0.255");
    private static final ObisCode EMERGENCY_CREDIT = ObisCode.fromString("0.0.19.10.1.255");

    private static final int COMMODITY_OBIS_CLASS_ID = 3;
    private static final ObisCode COMMODITY_OBIS_CODE = ObisCode.fromString("7.0.3.1.0.255");
    private static final int COMMODITY_OBIS_ATTRIBUT_INDEX = 3;

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
            updateCreditAmount(pendingMessage);
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
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN)) {
            getProtocol().getDlmsSession().getCosemObjectFactory().getDisconnector().remoteDisconnect();
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE)) {
            getProtocol().getDlmsSession().getCosemObjectFactory().getDisconnector().remoteReconnect();
        } else {   //Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    private ObisCode getCreditTypeObiscode(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        String description = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.creditType);
        int creditNo = CreditDeviceMessage.CreditType.entryForDescription(description).getId();
        switch (creditNo) {
            case 1:
                return EMERGENCY_CREDIT;
            default:
                return IMPORT_CREDIT;
        }
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

    private void updateCreditAmount(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode chargeObisCode = getCreditTypeObiscode(pendingMessage);
        CreditSetup chargeSetup = getCosemObjectFactory().getCreditSetup(chargeObisCode);
        Integer creditAmount = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.creditAmount));
        chargeSetup.invokeCreditMethod(CreditSetupMethods.UPDATE_AMOUNT, new Integer32(creditAmount));
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