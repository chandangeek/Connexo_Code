package com.energyict.protocolimplv2.dlms.idis.am540.messages;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributeobjects.ImageTransferStatus;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130MessageExecutor;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.enums.LoadProfileOptInOut;
import com.energyict.protocolimplv2.messages.enums.SetDisplayMode;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.DSMR50ActivitiyCalendarController;

import java.io.IOException;
import java.math.BigDecimal;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitorInstanceAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.thresholdInAmpereAttributeName;

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
                setDiplayOnOff(pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.WRITE_MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES)) {
                collectedMessage = writeMeasurementPeriod3ForInstantaneousValues(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LogBookDeviceMessage.ResetSecurityGroupEventCounterObjects)) {
                collectedMessage = resetSecurityEventCounterObjects(collectedMessage, pendingMessage);
            } else if (pendingMessage.getSpecification().equals(LogBookDeviceMessage.ResetAllSecurityGroupEventCounters)) {
                collectedMessage = resetAllSecurityEventCounters(collectedMessage, pendingMessage);
            } else {
                collectedMessage = super.executeMessage(pendingMessage, collectedMessage);
            }
        }
        return collectedMessage;
    }

    private CollectedMessage enableImageTransfer(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        try {
            ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();
            imageTransfer.enableImageTransfer();
        } catch (IOException e) {
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
            String errorMsg = "The ImageTransfer is in an invalid state: expected state '1' (Image transfer initiated), but was '" +
                    imageTransferStatus.getValue() + "' (" + imageTransferStatus.getInfo() + "). " +
                    "The verification and activation will not be executed.";
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, errorMsg));
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
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

        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(LOAD_PROFILE_CONTROL_SCHEDULE_OBISCODE);
        sas.writeExecutedScript(scriptStruct);
    }

    private void setDiplayOnOff(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        String modeName = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.setDisplayOnOffModeAttributeName).getDeviceMessageAttributeValue();
        int modeId = SetDisplayMode.fromModeName(modeName);

        final Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(LOAD_PROFILE_DISPLAY_CONTROL_SCRIPT_TABLE.getLN()));
        scriptStruct.addDataType(new Unsigned16(modeId));

        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(LOAD_PROFILE_DISPLAY_CONTROL_SCHEDULE_OBISCODE);
        sas.writeExecutedScript(scriptStruct);
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
                String errorMsg = "Selected security group event counter " + securityGroupEventCounter + " with obisCode = " + securityGroupEventObis + " is not present in device object list. " + e.getMessage();
                collectedMessage.setDeviceProtocolInformation(errorMsg);
                collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, errorMsg));
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                break;
            } catch (IOException e) {
                String errorMsg = "Resetting " + securityGroupEventCounter + " with obisCode = " + securityGroupEventObis + " back to 0, failed. " + e.getMessage();
                collectedMessage.setDeviceProtocolInformation(errorMsg);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, errorMsg));
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                break;
            }
        }
        return collectedMessage;
    }

    private CollectedMessage writeMeasurementPeriod3ForInstantaneousValues(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        try {
            Data data = getCosemObjectFactory().getData(MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES_OBIS);
            long value = data.getValue();
            data.setValueAttr(new Unsigned32(value));
        } catch (NotInObjectListException e) {
            String errorMsg = "Object identified by obisCode: " + MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES_OBIS + " is not present in device object list. " + e.getMessage();
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, errorMsg));
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        } catch (IOException e) {
            String errorMsg = "Exception occurred while trying to write a new value for object with obisCode: " + MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES_OBIS + ". " + e.getMessage();
            collectedMessage.setDeviceProtocolInformation(errorMsg);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, errorMsg));
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        }
        return collectedMessage;
    }
}