/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am540.messages;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.attributeobjects.ImageTransferStatus;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130MessageExecutor;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;

import java.io.IOException;


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

    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor;

    public AM540MessageExecutor(AbstractDlmsProtocol protocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, CollectedDataFactory collectedDataFactory) {
        super(protocol, issueService, readingTypeUtilService, collectedDataFactory);
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
            if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE)) {
                collectedMessage = verifyAndActivateFirmware(pendingMessage, collectedMessage);
//            } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.ENABLE_IMAGE_TRANSFER)) {
//                collectedMessage = enableImageTransfer(collectedMessage, pendingMessage);
//            } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.LOAD_BALANCING_CONFIGURE_SUPERVISION_MONITOR)) {
//                collectedMessage = updateSupervisionMonitor(collectedMessage, pendingMessage);
//            } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.LOAD_PROFILE_OPT_IN_OUT)) {
//                loadProfileOptInOUT(pendingMessage);
//            } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.SET_DISPLAY_ON_OFF)) {
//                setDiplayOnOff(pendingMessage);
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

//    @Override
//    protected ActivityCalendarController getActivityCalendarController() {
//        return new DSMR50ActivitiyCalendarController(getCosemObjectFactory(), getProtocol().getTimeZone(), true);
//    }

    private PLCConfigurationDeviceMessageExecutor getPLCConfigurationDeviceMessageExecutor() {
        if (plcConfigurationDeviceMessageExecutor == null) {
            plcConfigurationDeviceMessageExecutor = new PLCConfigurationDeviceMessageExecutor(getProtocol().getDlmsSession(), getProtocol().getOfflineDevice(), getCollectedDataFactory(), getClock(), getIssueService());
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

//    private CollectedMessage updateSupervisionMonitor(CollectedMessage collectedMessage, OfflineDeviceMessage offlineDeviceMessage) throws IOException {
//        int monitorInstance = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, monitorInstanceAttributeName).getDeviceMessageAttributeValue()).intValue();
//        long threshold = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, thresholdInAmpereAttributeName).getDeviceMessageAttributeValue()).longValue();
//        return updateThresholds(collectedMessage, offlineDeviceMessage, monitorInstance, threshold);
//    }

//    private void loadProfileOptInOUT(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
//        String scriptName = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, loadProfileOptInOutModeAttributeName).getDeviceMessageAttributeValue();
//
//        int scriptId = LoadProfileOptInOut.fromScriptName(scriptName);
//
//        final Structure scriptStruct = new Structure();
//        scriptStruct.addDataType(new OctetString(LOAD_PROFILE_CONTROL_SCRIPT_TABLE.getLN()));
//        scriptStruct.addDataType(new Unsigned16(scriptId));
//
//        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(LOAD_PROFILE_CONTROL_SCHEDULE_OBISCODE);
//        sas.writeExecutedScript(scriptStruct);
//    }
//
//    private void setDiplayOnOff(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
//        String modeName = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.setDisplayOnOffModeAttributeName).getDeviceMessageAttributeValue();
//        int modeId = SetDisplayMode.fromModeName(modeName);
//
//        final Structure scriptStruct = new Structure();
//        scriptStruct.addDataType(new OctetString(LOAD_PROFILE_DISPLAY_CONTROL_SCRIPT_TABLE.getLN()));
//        scriptStruct.addDataType(new Unsigned16(modeId));
//
//        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(LOAD_PROFILE_DISPLAY_CONTROL_SCHEDULE_OBISCODE);
//        sas.writeExecutedScript(scriptStruct);
//    }
}
