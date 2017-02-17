package com.energyict.protocolimplv2.dlms.idis.am540.messages;

import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;

import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.attributeobjects.ImageTransferStatus;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NotInObjectListException;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.actionWhenOverThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.actionWhenUnderThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitorInstanceAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitoredValueAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newWrappedEncryptionKeyAttributeName;
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

    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor;

    public AM540MessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
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
            } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS)) {
                changeEncryptionKeyAndUseNewKey(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT)) {
                changeEncryptionKeyAndUseNewKey(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.BILLING_RESET)) {
                collectedMessage = billingReset(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_ATTRIBUTES_4TO9)) {
                collectedMessage = configureLoadLimitParametersEVN_Attributes_4to9(collectedMessage, pendingMessage);
            } else {
                collectedMessage = super.executeMessage(pendingMessage, collectedMessage);
            }
        }
        return collectedMessage;
    }

    private CollectedMessage configureLoadLimitParametersEVN_Attributes_4to9(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        long normalThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, normalThresholdAttributeName).getDeviceMessageAttributeValue()).longValue();
        long emergencyThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyThresholdAttributeName).getDeviceMessageAttributeValue()).longValue();
        int overThresholdDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, overThresholdDurationAttributeName).getDeviceMessageAttributeValue());
        int underThresholdDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, underThresholdDurationAttributeName).getDeviceMessageAttributeValue());
        int emergencyProfileId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileIdAttributeName).getDeviceMessageAttributeValue());
        String emergencyProfileGroupIdList = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileGroupIdListAttributeName).getDeviceMessageAttributeValue();
        Date emergencyProfileActivationDate = new Date(new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileActivationDateAttributeName).getDeviceMessageAttributeValue()).longValue());
        int emergencyProfileDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileDurationAttributeName).getDeviceMessageAttributeValue());

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

    protected CollectedMessage verifyAndActivateFirmware(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();

        ImageTransferStatus imageTransferStatus = imageTransfer.readImageTransferStatus();
        if (imageTransferStatus.equals(ImageTransferStatus.TRANSFER_INITIATED)) {
            try {
                imageTransfer.verifyAndPollForSuccess();
            } catch (DataAccessResultException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String errorMsg = "Verification of image failed: " + e.getMessage();
                collectedMessage.setDeviceProtocolInformation(errorMsg);
                collectedMessage.setFailureInformation(ResultType.DataIncomplete, createMessageFailedIssue(pendingMessage, errorMsg));
                return collectedMessage;
            }

            try {
                imageTransfer.setUsePollingVerifyAndActivate(false);    //Don't use polling for the activation, the meter reboots immediately!
                imageTransfer.imageActivation();
                collectedMessage.setDeviceProtocolInformation("Image has been activated.");
            } catch (IOException e) {
                if (isTemporaryFailure(e) || isTemporaryFailure(e.getCause())) {
                    collectedMessage.setDeviceProtocolInformation("Image activation returned 'temporary failure'. The activation is in progress, moving on.");
                } else if (e.getMessage().toLowerCase().contains("timeout")) {
                    collectedMessage.setDeviceProtocolInformation("Image activation timed out, meter is rebooting. Moving on.");
                } else {
                    throw e;
                }
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

    @Override
    protected ActivityCalendarController getActivityCalendarController() {
        return new DSMR50ActivitiyCalendarController(getCosemObjectFactory(), getProtocol().getTimeZone(), true);
    }

    private PLCConfigurationDeviceMessageExecutor getPLCConfigurationDeviceMessageExecutor() {
        if (plcConfigurationDeviceMessageExecutor == null) {
            plcConfigurationDeviceMessageExecutor = new PLCConfigurationDeviceMessageExecutor(getProtocol().getDlmsSession(), getProtocol().getOfflineDevice());
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
        int monitorInstance = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, monitorInstanceAttributeName).getDeviceMessageAttributeValue()).intValue();
        long threshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, thresholdInAmpereAttributeName).getDeviceMessageAttributeValue()).longValue();
        return updateThresholds(collectedMessage, offlineDeviceMessage, monitorInstance, threshold);
    }

    private void loadProfileOptInOUT(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        String scriptName = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.loadProfileOptInOutModeAttributeName).getDeviceMessageAttributeValue();

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
        String modeName = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.setDisplayOnOffModeAttributeName).getDeviceMessageAttributeValue();
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
        String securityGroupEventCounter = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.securityGroupEventCounters).getDeviceMessageAttributeValue();
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
            long value = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.NewValueAttributeName).getDeviceMessageAttributeValue());
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

        String monitoredValueObis_Attribute = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, monitoredValueAttributeName).getDeviceMessageAttributeValue();
        long normalThreshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, normalThresholdAttributeName).getDeviceMessageAttributeValue()).longValue();
        int overThresholdDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, overThresholdDurationAttributeName).getDeviceMessageAttributeValue());
        int underThresholdDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, underThresholdDurationAttributeName).getDeviceMessageAttributeValue());
        int actionWhenUnderThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, actionWhenUnderThresholdAttributeName).getDeviceMessageAttributeValue());
        int actionWhenOverThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, actionWhenOverThresholdAttributeName).getDeviceMessageAttributeValue());

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
        String newKey = getDeviceMessageAttributeValue(pendingMessage, newEncryptionKeyAttributeName);
        changeKeyAndUseNewKey(pendingMessage, SecurityMessage.KeyID.GLOBAL_UNICAST_ENCRYPTION_KEY.getId(), newWrappedEncryptionKeyAttributeName);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(ProtocolTools.getBytesFromHexString(newKey, ""));

        int clientInUse = getProtocol().getDlmsSession().getProperties().getClientMacAddress();
        int clientToChangeKeyFor = getClientId(pendingMessage);

        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
        if(clientInUse == clientToChangeKeyFor){
            securityContext.setFrameCounter(1);
        } else {
            ((AM540Cache)getProtocol().getDeviceCache()).setTXFrameCounter(clientToChangeKeyFor, 1);
        }
        securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);

        return collectedMessage;
    }
}