package com.energyict.protocolimplv2.dlms.acud.messages;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.ChargeSetupAttributes;
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
import com.energyict.protocolimplv2.messages.ChargeDeviceMessage;
import com.energyict.protocolimplv2.messages.CreditDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class AcudMessageExecutor extends AbstractMessageExecutor {


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
        if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE)) {
            upgradeFirmware(pendingMessage);
            // clock
        } else if (pendingMessage.getSpecification().equals(CreditDeviceMessage.UPDATE_CREDIT_AMOUNT)) {
            updateCreditAmount(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.ACTIVATE_PASSIVE_UNIT_CHARGE)) {
            activatePassiveUnitCharge(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION)) {
            changePassiveUnitChargeWithActivation(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION_DATE)) {
            changePassiveUnitChargeWithActivationDate(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.UPDATE_UNIT_CHARGE)) {
            updateUnitCharge(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_CHARGE_PERIOD)) {
            changeChargePeriod(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ChargeDeviceMessage.CHANGE_CHARGE_PROPORTION)) {
            changeChargeProportion(pendingMessage);
        } else {   //Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    private ObisCode getCreditTypeObiscode(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        String description = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.creditTypeAttributeName);
        int creditNo = CreditDeviceMessage.CreditType.entryForDescription(description).getId();
        switch (creditNo) {
            case 1:
                return EMERGENCY_CREDIT;
            default:
                return IMPORT_CREDIT;
        }
    }

    private ObisCode getChargeTypeObiscode(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        String description = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.chargeTypeAttributeName);
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

    private void updateCreditAmount(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode chargeObisCode = getCreditTypeObiscode(pendingMessage);
        CreditSetup chargeSetup = getCosemObjectFactory().getCreditSetup(chargeObisCode);
        Integer creditAmount = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.creditAmount));
        chargeSetup.invokeCreditMethod(CreditSetupMethods.UPDATE_AMOUNT, new Integer32(creditAmount));
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
        OctetString activationTimeInSec = createActivationTimeInSec(pendingMessage);
        chargeSetup.writeChargeAttribute(ChargeSetupAttributes.UNIT_CHARGE_ACTIVATION_TIME, activationTimeInSec);
        Structure passiveUnitChargeStructure = createPassiveUnitCharge(pendingMessage);
        chargeSetup.writeChargeAttribute(ChargeSetupAttributes.UNIT_CHARGE_PASSIVE, passiveUnitChargeStructure);
    }

    private OctetString createActivationTimeInSec(OfflineDeviceMessage pendingMessage) {
        String passiveUnitChargeActivationTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.passiveUnitChargeActivationTime).getValue();
        Calendar activationTime = Calendar.getInstance(getProtocol().getTimeZone());
        activationTime.setTimeInMillis(Long.valueOf(passiveUnitChargeActivationTime));
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
            String chargeTableTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, CHARGE_TABLE_TIME + "i").getValue();
            chargeTableElement.addDataType(OctetString.fromString(chargeTableTime));
            Integer chargeTableUnit = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, CHARGE_TABLE_UNIT + "i"));
            chargeTableElement.addDataType(new Integer16(chargeTableUnit));
            passiveUnitChargeTable.addDataType(chargeTableElement);
        }
        passiveUnitChargeElement2.addDataType(passiveUnitChargeTable);
    }

    private void updateUnitCharge(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode chargeObisCode = getChargeTypeObiscode(pendingMessage);
        getCosemObjectFactory().getChargeSetup(chargeObisCode).invokeChargeMethod(ChargeSetupMethods.UPDATE_UNIT_CHARGE);
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

        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();
        imageTransfer.setBooleanValue(getBooleanValue());
        imageTransfer.setUsePollingVerifyAndActivate(true);     //Use polling to check the result of the image verification
        imageTransfer.upgrade(binaryImage, false);

        try {
            imageTransfer.setUsePollingVerifyAndActivate(false);    //Don't use polling for the activation, the meter reboots immediately!
            imageTransfer.imageActivation();
        } catch (IOException e) {
            if (isTemporaryFailure(e) || isTemporaryFailure(e.getCause()) || isHardwareFault(e) || isHardwareFault(e.getCause())) {
                //Move on in case of temporary failure/hardware fault,
                return;
            } else {
                throw e;
            }
        }
    }

    private boolean isTemporaryFailure(Throwable e) {
        if (e == null) {
            return false;
        } else if (e instanceof DataAccessResultException) {
            return (((DataAccessResultException) e).getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
        } else {
            return false;
        }
    }

    private boolean isHardwareFault(Throwable e) {
        if (e == null) {
            return false;
        } else if (e instanceof DataAccessResultException) {
            return ((DataAccessResultException) e).getDataAccessResult() == DataAccessResultCode.HARDWARE_FAULT.getResultCode();
        } else {
            return false;
        }
    }

    /**
     * Default value, subclasses can override. This value is used to set the image_transfer_enable attribute.
     */
    protected int getBooleanValue() {
        return 0xFF;
    }
}
