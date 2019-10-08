package com.energyict.protocolimplv2.dlms.idis.am540.messages;

import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.attributeobjects.ImageTransferStatus;
import com.energyict.dlms.exceptionhandler.ExceptionResponseException;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130MessageExecutor;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540Cache;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.enums.LoadProfileOptInOut;
import com.energyict.protocolimplv2.messages.enums.SetDisplayMode;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.DSMR50ActivitiyCalendarController;
import com.energyict.sercurity.KeyRenewalInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.actionWhenOverThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.actionWhenUnderThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.adHocEndOfBillingActivationDatedAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitorInstanceAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitoredValueAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPSKAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.normalThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.thresholdInAmpereAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.underThresholdDurationAttributeName;

/**
 * @author sva
 * @since 11/08/2015 - 16:14
 */
public class AM540MessageExecutor extends AM130MessageExecutor {

    private static final int MAX_MBUS_SLAVES = 4;
    private static final ObisCode LOAD_PROFILE_CONTROL_SCHEDULE_OBISCODE = ObisCode.fromString("0.0.15.0.5.255");
    private static final ObisCode LOAD_PROFILE_CONTROL_SCRIPT_TABLE = ObisCode.fromString("0.0.10.0.109.255");
    private static final ObisCode LOAD_PROFILE_DISPLAY_CONTROL_SCHEDULE_OBISCODE = ObisCode.fromString("0.0.15.0.9.255");
    private static final ObisCode LOAD_PROFILE_DISPLAY_CONTROL_SCRIPT_TABLE = ObisCode.fromString("0.0.10.0.113.255");
    private static final ObisCode MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES_OBIS = ObisCode.fromString("1.0.0.8.2.255");
    private static final ObisCode BILLING_SCRIPT_TABLE_OBIS_CODE = ObisCode.fromString("0.0.10.0.1.255");
    private static final ObisCode ADHOC_END_OF_BILLING = ObisCode.fromString("0.0.15.1.0.255");
    private static final ObisCode PSK_RENEWAL_OBISCODE = ObisCode.fromString("0.0.94.33.128.255");

    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor;

    public AM540MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    protected int getMaxMBusSlaves() {
        return MAX_MBUS_SLAVES;
    }

    @Override
    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        CollectedMessage plcMessageResult = getPLCConfigurationDeviceMessageExecutor().executePendingMessage(pendingMessage, collectedMessage);
        if (plcMessageResult != null) {
            collectedMessage = plcMessageResult;
        } else { // if it was not a PLC message
            if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.VerifyAndActivateFirmware)) {
                collectedMessage = verifyAndActivateFirmware(pendingMessage, collectedMessage);
            } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.ENABLE_IMAGE_TRANSFER)) {
                collectedMessage = enableImageTransfer(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.UPDATE_SUPERVISION_MONITOR)) {
                collectedMessage = updateSupervisionMonitor(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.LOAD_PROFILE_OPT_IN_OUT)) {
                loadProfileOptInOUT(pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.SET_DISPLAY_ON_OFF)) {
                setDisplayOnOff(pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.WRITE_MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES)) {
                collectedMessage = writeMeasurementPeriod3ForInstantaneousValues(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LogBookDeviceMessage.ResetSecurityGroupEventCounterObjects)) {
                collectedMessage = resetSecurityEventCounterObjects(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LogBookDeviceMessage.ResetAllSecurityGroupEventCounters)) {
                collectedMessage = resetAllSecurityEventCounters(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_EXCEPT_EMERGENCY_ONES)) {
                collectedMessage = configureLoadLimitParametersExceptEmergencyOnes(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY)) {
                changeEncryptionKeyAndUseNewKey(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY_FOR_PREDEFINED_CLIENT)) {
                changeEncryptionKeyAndUseNewKey(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.BILLING_RESET)) {
                collectedMessage = billingReset(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_ATTRIBUTES_4TO9)) {
                collectedMessage = configureLoadLimitParametersEVN_Attributes_4to9(collectedMessage, pendingMessage);
            }else if (pendingMessage.getSpecification().equals(DeviceActionMessage.BillingResetWithActivationDate)) {
                collectedMessage = billingResetWithActivationDate(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.ENABLE_AND_INITIATE_IMAGE_TRANSFER)) {
                this.enableAndInitiateImageTransfer(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.CONFIGURABLE_IMAGE_TRANSFER_WITH_RESUME_OPTION)) {
                this.executeImageTransferActions(pendingMessage);
            } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.ReadDLMSAttribute)) {
                collectedMessage = this.readDlmsAttribute(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_PSK_WITH_NEW_KEYS)) {
                byte[] wrappedKey = getWrappedKey(pendingMessage, newPSKAttributeName);
                changePSK(wrappedKey);
            } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_PSK_USING_SERVICE_KEY)) {
                collectedMessage = this.changePSKUsingServiceKey(pendingMessage, collectedMessage);
            } else {
                collectedMessage = super.executeMessage(pendingMessage, collectedMessage);
            }
        }

        return collectedMessage;
    }

    protected void changePSK(byte[] newWrappedPSK) throws IOException {
        Data pskRenewalObject = getProtocol().getDlmsSession().getCosemObjectFactory().getData(PSK_RENEWAL_OBISCODE);
        try {
            pskRenewalObject.setValueAttr(OctetString.fromByteArray(newWrappedPSK));
        } catch (ExceptionResponseException e) {
            //Swallow this exception. It is the Beacon that responds an error because the logical device of the meter no longer exists.
            //Indeed, this is expected behaviour because the meter disconnects immediately after the PSK is written.
            ;
        }
    }

    //Sub classes can override this implementation
    protected CollectedMessage changePSKUsingServiceKey(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        throw new ProtocolException("Service keys can only be injected by the HSM crypto-protocol");
    }

    private CollectedMessage readDlmsAttribute(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        String obisCodeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getValue();
        int attributeId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.attributeId).getValue());
        int classId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.classId).getValue());

        obisCodeString = obisCodeString.replace(":", ".").replace("-", ".").replace(" ", "");
        ObisCode obisCode = ObisCode.fromString(obisCodeString);

        DLMSAttribute dlmsAttribute = new DLMSAttribute(obisCode, attributeId, classId);

        try {
            ComposedCosemObject composeObject = getCosemObjectFactory().getComposedCosemObject(dlmsAttribute);
            AbstractDataType abstractDataType = composeObject.getAttribute(dlmsAttribute);
            collectedMessage.setDeviceProtocolInformation(abstractDataType.toString());
        } catch (IOException e) {
            e.printStackTrace();
            collectedMessage.setDeviceProtocolInformation(e.toString());
        }

        return collectedMessage;
    }

    private CollectedMessage configureLoadLimitParametersEVN_Attributes_4to9(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        long normalThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, normalThresholdAttributeName).getValue()).longValue();
        long emergencyThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyThresholdAttributeName).getValue()).longValue();
        int overThresholdDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, overThresholdDurationAttributeName).getValue());
        int underThresholdDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, underThresholdDurationAttributeName).getValue());
        int emergencyProfileId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileIdAttributeName).getValue());
        String emergencyProfileGroupIdList = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileGroupIdListAttributeName).getValue();
        Date emergencyProfileActivationDate = new Date(new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileActivationDateAttributeName).getValue()).longValue());
        int emergencyProfileDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileDurationAttributeName).getValue());

        try {

            Limiter limiter = getCosemObjectFactory().getLimiter();
            writeNormalThreshold(normalThreshold, limiter);
            writeEmergencyThreshold(emergencyThreshold, limiter);
            limiter.writeMinOverThresholdDuration(new Unsigned32(overThresholdDuration));
            limiter.writeMinUnderThresholdDuration(new Unsigned32(underThresholdDuration));
            writeEmergencyProfile(emergencyProfileId, emergencyProfileActivationDate, emergencyProfileDuration, limiter);
            Array groupIdList = new Array();
            String[] profile_id_list = emergencyProfileGroupIdList.split(",");
            for (String id : profile_id_list) {
                groupIdList.addDataType(new Unsigned16(Integer.parseInt(id)));
            }
            limiter.writeEmergencyProfileGroupIdList(groupIdList);
        } catch (NotInObjectListException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            setNotInObjectListMessage(collectedMessage, Limiter.getDefaultObisCode().getValue(), pendingMessage, e);
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String errorMsg = "Exception occurred while trying to write the action scripts for object with obisCode: " + Limiter.getDefaultObisCode() + ". " + e.getMessage();
            setIncompatibleFailedMessage(collectedMessage, pendingMessage, errorMsg);
        }

        return collectedMessage;
    }

    private void writeEmergencyThreshold(long activeThreshold, Limiter limiter) throws IOException {
        limiter.writeThresholdEmergency(new Unsigned32(activeThreshold)); //TODO check if this type will be always accepted or the register value type should be used
    }

    private CollectedMessage billingReset(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        try {
            ScriptTable demandResetScriptTable = getCosemObjectFactory().getScriptTable(BILLING_SCRIPT_TABLE_OBIS_CODE);
            demandResetScriptTable.execute(1);
            collectedMessage.setDeviceProtocolInformation("Billing reset successfully performed");
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String errorMsg = "Failed to perform billing reset: " + e.getMessage();
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, errorMsg));
        }
        return collectedMessage;
    }


    private CollectedMessage billingResetWithActivationDate(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        try {
            String activationEpochString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, adHocEndOfBillingActivationDatedAttributeName).getValue();
            SingleActionSchedule adHocEndOfBilling = getCosemObjectFactory().getSingleActionSchedule(ADHOC_END_OF_BILLING);

            Array executionTime = convertEpochToDateTimeArray(activationEpochString);

            adHocEndOfBilling.writeExecutionTime(executionTime);

            String protocolInfo = activationEpochString;
            try {
                Date activationDate = new Date(Long.parseLong(activationEpochString));
                protocolInfo = activationDate.toString();
            } catch (Exception ex) {
                // swallow
            }

            collectedMessage.setDeviceProtocolInformation("Added a new ad-hoc end-of-billing reset to " + protocolInfo);
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String errorMsg = "Failed to add an ad-hoc billing reset: " + e.getMessage();
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, errorMsg));
        }
        return collectedMessage;
    }


    private CollectedMessage enableImageTransfer(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        try {
            ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();
            imageTransfer.enableImageTransfer();
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String errorMsg = "Failed to enable image transfer: " + e.getMessage();
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, errorMsg));
        }
        return collectedMessage;
    }

    /**
     * Enable and initiate the image transfer.
     *
     * @param collectedMessage The output {@link CollectedMessage}.
     * @param pendingMessage The input {@link OfflineDeviceMessage}.
     */
    private final void enableAndInitiateImageTransfer(final CollectedMessage collectedMessage, final OfflineDeviceMessage pendingMessage) {
        final String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName)
                .getValue();
        final String imageSizeAttributeValue = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.FW_UPGRADE_IMAGE_SIZE)
                .getValue();

        if (imageIdentifier != null && imageIdentifier.length() > 0 && imageSizeAttributeValue != null && imageSizeAttributeValue.length() > 0) {
            final long imageSize = new BigDecimal(imageSizeAttributeValue).longValue();

            try {
                final ImageTransfer imageTransfer = this.getCosemObjectFactory().getImageTransfer();
                imageTransfer.enableImageTransfer();

                final Structure structure = new Structure();

                structure.addDataType(OctetString.fromString(imageIdentifier));
                structure.addDataType(new Unsigned32(imageSize));

                imageTransfer.imageTransferInitiate(structure);
            } catch (IOException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                if (this.getLogger().isLoggable(Level.WARNING)) {
                    this.getLogger().log(Level.WARNING, "Error enabling and initiating image transfer : [" + e.getMessage() + "]", e);
                }

                final String errorMessage = new StringBuilder("Could not enable and initiate image transfer : [").append(e.getMessage()).append("]").toString();
                collectedMessage.setDeviceProtocolInformation(errorMessage);
                collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, errorMessage));
            }
        } else {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);

            final String errorMessage = new StringBuilder("Message contents not valid : need an image identifier and image size attribute, image identifier was [")
                    .append(imageIdentifier)
                    .append("], image size [")
                    .append(imageSizeAttributeValue).append("]")
                    .toString();

            collectedMessage.setDeviceProtocolInformation(errorMessage);
            collectedMessage.setFailureInformation(ResultType.Other, this.createMessageFailedIssue(pendingMessage, errorMessage));
        }
    }

    protected CollectedMessage verifyAndActivateFirmware(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {

        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getValue();
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();

        ImageTransferStatus imageTransferStatus = imageTransfer.readImageTransferStatus();
        if (imageTransferStatus.getValue() >= ImageTransferStatus.TRANSFER_INITIATED.getValue()) {
            if (imageTransferStatus.equals(ImageTransferStatus.VERIFICATION_FAILED) || imageTransferStatus.equals(ImageTransferStatus.TRANSFER_INITIATED)) {
            try {
                imageTransfer.verifyAndPollForSuccess();
            } catch (DataAccessResultException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String errorMsg = "Verification of image failed: " + e.getMessage();
                collectedMessage.setDeviceProtocolInformation(errorMsg);
                    collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, errorMsg));
                return collectedMessage;
            }
            } else {
                collectedMessage.setDeviceProtocolInformation(collectedMessage.getDeviceProtocolInformation() + " Image verification action not executed because current Image Transfer Status was " + imageTransferStatus
                        .getInfo() + ".");
            }

            imageTransferStatus = imageTransfer.readImageTransferStatus();
            if (imageTransferStatus.equals(ImageTransferStatus.VERIFICATION_SUCCESSFUL)) {
            try {
                    if (activationDate.isEmpty()) {
                imageTransfer.setUsePollingVerifyAndActivate(false);    //Don't use polling for the activation, the meter reboots immediately!
                imageTransfer.imageActivation();
                collectedMessage.setDeviceProtocolInformation("Image has been activated.");
                    } else {
                        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
                        sas.writeExecutionTime(convertLongDateToDlmsArray(Long.valueOf(activationDate)));
                    }
            } catch (IOException e) {

                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                if (isTemporaryFailure(e) || isTemporaryFailure(e.getCause())) {
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
                    collectedMessage.setDeviceProtocolInformation("Image activation returned 'temporary failure'. The activation is in progress, moving on.");
                        return collectedMessage;
                } else if (e.getMessage().toLowerCase().contains("timeout")) {
                    collectedMessage.setDeviceProtocolInformation("Image activation timed out, meter is rebooting. Moving on.");
                        collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, collectedMessage.getDeviceProtocolInformation()));
                } else {
                    throw e;
                }
            }
        } else {
                collectedMessage.setDeviceProtocolInformation(collectedMessage.getDeviceProtocolInformation() + " Image activation action not executed because current Image Transfer Status was: " + imageTransferStatus
                        .getInfo() + ".");
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, collectedMessage.getDeviceProtocolInformation()));
            }
        } else {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String errorMsg = "The ImageTransfer is in an invalid state: expected state '1' (Image transfer initiated), but was '" +
                    imageTransferStatus.getValue() + "' (" + imageTransferStatus.getInfo() + "). " +
                    "The verification and activation will not be executed.";
            setIncompatibleFailedMessage(collectedMessage, pendingMessage, errorMsg);
        }

        return collectedMessage;
    }

    protected Array convertLongDateToDlmsArray(Long epoch) {
        Date actionTime = new Date(epoch);
        Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
        cal.setTime(actionTime);
        return convertDateToDLMSArray(cal);
    }

    @Override
    protected ActivityCalendarController getActivityCalendarController() {
        return new DSMR50ActivitiyCalendarController(getCosemObjectFactory(), getProtocol().getTimeZone(), true);
    }

    protected PLCConfigurationDeviceMessageExecutor getPLCConfigurationDeviceMessageExecutor() {
        if (plcConfigurationDeviceMessageExecutor == null) {
            plcConfigurationDeviceMessageExecutor = new PLCConfigurationDeviceMessageExecutor(getProtocol().getDlmsSession(), getProtocol().getOfflineDevice(), getCollectedDataFactory(), getIssueFactory());
        }
        return plcConfigurationDeviceMessageExecutor;
    }

    protected boolean isTemporaryFailure(Throwable e) {
        if (e == null) {
            return false;
        } else if (e instanceof DataAccessResultException) {
            return (((DataAccessResultException) e).getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
        } else {
            return false;
        }
    }

    private CollectedMessage updateSupervisionMonitor(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int monitorInstance = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, monitorInstanceAttributeName).getValue()).intValue();
        long threshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, thresholdInAmpereAttributeName).getValue()).longValue();
        return updateThresholds(collectedMessage, offlineDeviceMessage, monitorInstance, threshold);
    }

    private void loadProfileOptInOUT(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        String scriptName = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.loadProfileOptInOutModeAttributeName).getValue();

        int scriptId = LoadProfileOptInOut.fromScriptName(scriptName);

        final Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(LOAD_PROFILE_CONTROL_SCRIPT_TABLE.getLN()));
        scriptStruct.addDataType(new Unsigned16(scriptId));

        getProtocol().getLogger().info("Writing Load profile control schedule in {"+LOAD_PROFILE_CONTROL_SCHEDULE_OBISCODE+"}: "+scriptStruct.toString());
        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(LOAD_PROFILE_CONTROL_SCHEDULE_OBISCODE);
        sas.writeExecutedScript(scriptStruct);

        getProtocol().getLogger().info("Executing script activation in {"+LOAD_PROFILE_CONTROL_SCRIPT_TABLE+"}, scriptId="+scriptId);
        ScriptTable loadProfileControlScriptTable = getCosemObjectFactory().getScriptTable(LOAD_PROFILE_CONTROL_SCRIPT_TABLE);
        loadProfileControlScriptTable.execute(scriptId);

        getProtocol().getLogger().info("Load Profile Opt In/Out ended successfully.");
    }

    private void setDisplayOnOff(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        String modeName = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.setDisplayOnOffModeAttributeName).getValue();
        int modeId = SetDisplayMode.fromModeName(modeName);

        final Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(LOAD_PROFILE_DISPLAY_CONTROL_SCRIPT_TABLE.getLN()));
        scriptStruct.addDataType(new Unsigned16(modeId));

        getProtocol().getLogger().info("Writing Load profile display control {"+LOAD_PROFILE_DISPLAY_CONTROL_SCHEDULE_OBISCODE+"}: "+scriptStruct.toString());
        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(LOAD_PROFILE_DISPLAY_CONTROL_SCHEDULE_OBISCODE);
        sas.writeExecutedScript(scriptStruct);

        getProtocol().getLogger().info("Executing script activation in {"+LOAD_PROFILE_DISPLAY_CONTROL_SCRIPT_TABLE+"}, modeId="+modeId);
        ScriptTable loadProfileControlScriptTable = getCosemObjectFactory().getScriptTable(LOAD_PROFILE_DISPLAY_CONTROL_SCRIPT_TABLE);
        loadProfileControlScriptTable.execute(modeId);

        getProtocol().getLogger().info("Load Profile On/Off ended successfully.");
    }

    private CollectedMessage resetSecurityEventCounterObjects(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        String securityGroupEventCounter = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.securityGroupEventCounters).getValue();
        LogBookDeviceMessage.SecurityEventCounter[] counters = new LogBookDeviceMessage.SecurityEventCounter[]{LogBookDeviceMessage.SecurityEventCounter.valueOf(securityGroupEventCounter)};
        return resetSecurityEventCounterValue(collectedMessage, pendingMessage, counters);
    }

    private CollectedMessage resetAllSecurityEventCounters(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        return resetSecurityEventCounterValue(collectedMessage, pendingMessage, LogBookDeviceMessage.SecurityEventCounter.values());
    }

    private CollectedMessage resetSecurityEventCounterValue(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage, LogBookDeviceMessage.SecurityEventCounter[] securityGroupEventCounters) {
        for (LogBookDeviceMessage.SecurityEventCounter securityGroupEventCounter : securityGroupEventCounters) {
            ObisCode securityGroupEventObis = ObisCode.fromString(securityGroupEventCounter.getObis());
            try {
                Data data = getCosemObjectFactory().getData(securityGroupEventObis);
                data.setValueAttr(new Unsigned16(0));
            } catch (NotInObjectListException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                setNotInObjectListMessage(collectedMessage, securityGroupEventObis.getValue(), pendingMessage, e);
                break;
            } catch (IOException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String errorMsg = "Resetting " + securityGroupEventCounter + " with obisCode = " + securityGroupEventObis + " back to 0, failed. " + e.getMessage();
                setIncompatibleFailedMessage(collectedMessage, pendingMessage, errorMsg);
                break;
            }
        }
        return collectedMessage;
    }

    private CollectedMessage writeMeasurementPeriod3ForInstantaneousValues(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        try {
            long value = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.NewValueAttributeName).getValue());
            Register register = getCosemObjectFactory().getRegister(MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES_OBIS);
            register.setValueAttr(new Unsigned32(value));
        } catch (NotInObjectListException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            setNotInObjectListMessage(collectedMessage, MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES_OBIS.getValue(), pendingMessage, e);
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String errorMsg = "Exception occurred while trying to write a new value for object with obisCode: " + MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES_OBIS + ". " + e.getMessage();
            setIncompatibleFailedMessage(collectedMessage, pendingMessage, errorMsg);
        }
        return collectedMessage;
    }

    private CollectedMessage configureLoadLimitParametersExceptEmergencyOnes(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {

        String monitoredValueObis_Attribute = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, monitoredValueAttributeName).getValue();
        long normalThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, normalThresholdAttributeName).getValue()).longValue();
        int overThresholdDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, overThresholdDurationAttributeName).getValue());
        int underThresholdDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, underThresholdDurationAttributeName).getValue());
        int actionWhenUnderThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, actionWhenUnderThresholdAttributeName).getValue());
        int actionWhenOverThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, actionWhenOverThresholdAttributeName).getValue());

        try {
            Limiter limiter = getCosemObjectFactory().getLimiter();
            setMonitoredValue(limiter, monitoredValueObis_Attribute);
            writeNormalThreshold(normalThreshold, limiter);
            limiter.writeMinOverThresholdDuration(new Unsigned32(overThresholdDuration));
            limiter.writeMinUnderThresholdDuration(new Unsigned32(underThresholdDuration));
            writeActions(actionWhenOverThreshold, actionWhenUnderThreshold, limiter);
        } catch (NotInObjectListException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            setNotInObjectListMessage(collectedMessage, Limiter.getDefaultObisCode().getValue(), pendingMessage, e);
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String errorMsg = "Exception occurred while trying to write the action scripts for object with obisCode: " + Limiter.getDefaultObisCode() + ". " + e.getMessage();
            setIncompatibleFailedMessage(collectedMessage, pendingMessage, errorMsg);
        }

        return collectedMessage;
    }

    private void writeActions(int actionOverThreshold, int actionUnderThreshold, Limiter limiter) throws IOException {
        Limiter.ActionItem overThresholdAction = limiter.new ActionItem(OctetString.fromByteArray(DISCONNECTOR_SCRIPT_OBISCODE.getLN(), 6), new Unsigned16(actionOverThreshold));
        Limiter.ActionItem underThresholdAction = limiter.new ActionItem(OctetString.fromByteArray(DISCONNECTOR_SCRIPT_OBISCODE.getLN(), 6), new Unsigned16(actionUnderThreshold));

        Limiter.ActionType actions = limiter.new ActionType(overThresholdAction, underThresholdAction);
        limiter.writeActions(actions);
    }

    protected void setMonitoredValue(Limiter limiter, String monitoredValueObisAndAttribute) throws IOException {
        String[] obis_attribute = monitoredValueObisAndAttribute.split(",");
        ObisCode obisCode = ObisCode.fromString(obis_attribute[0].trim());
        int attribute = Integer.parseInt(obis_attribute[1].trim());
        int classId = getCosemObjectFactory().getProtocolLink().getMeterConfig().getClassId(obisCode);

        Limiter.ValueDefinitionType vdt = limiter.new ValueDefinitionType();
        vdt.setAttribute(attribute);
        vdt.addDataType(new Unsigned16(classId));
        vdt.addDataType(OctetString.fromObisCode(obisCode));
        vdt.addDataType(new Integer8(attribute));
        limiter.writeMonitoredValue(vdt);
    }

    protected void writeNormalThreshold(long activeThreshold, Limiter limiter) throws IOException {
        limiter.writeThresholdNormal(new Unsigned32(activeThreshold)); //TODO check if this type will be always accepted or the register value type should be used
    }

    protected void setNotInObjectListMessage(CollectedMessage collectedMessage, String obiscode, OfflineDeviceMessage pendingMessage, NotInObjectListException e) {
        String errorMsg = "Object identified by obisCode: " + obiscode + " is not present in device object list. " + e.getMessage();
        collectedMessage.setDeviceProtocolInformation(errorMsg);
        collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, errorMsg));
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
    }

    protected void setIncompatibleFailedMessage(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage, String errorMsg) {
        collectedMessage.setDeviceProtocolInformation(errorMsg);
        collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, errorMsg));
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
    }

    @Override
    protected CollectedMessage changeEncryptionKeyAndUseNewKey(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) throws IOException {
        KeyRenewalInfo keyRenewalInfo = KeyRenewalInfo.fromJson(getDeviceMessageAttributeValue(pendingMessage, newEncryptionKeyAttributeName));
        byte[] newSymmetricKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.keyValue, "");
        byte[] wrappedKey = ProtocolTools.getBytesFromHexString(keyRenewalInfo.wrappedKeyValue, "");
        renewKeyForClient(wrappedKey, SecurityMessage.KeyID.GLOBAL_UNICAST_ENCRYPTION_KEY.getId(), getClientSecuritySetup(pendingMessage));

        int clientInUse = getProtocol().getDlmsSession().getProperties().getClientMacAddress();
        int clientToChangeKeyFor = getClientId(pendingMessage);

        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();

        if(clientInUse == clientToChangeKeyFor){
            securityContext.setFrameCounter(1);
            getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(newSymmetricKey);
        } else {
            ((AM540Cache)getProtocol().getDeviceCache()).setTXFrameCounter(clientToChangeKeyFor, 1);
        }
        securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);

        return collectedMessage;
    }

    /**
     * Returns the {@link Logger} to use.
     *
     * @return The {@link Logger}.
     */
    private final Logger getLogger() {
        return this.getProtocol().getLogger();
    }
}