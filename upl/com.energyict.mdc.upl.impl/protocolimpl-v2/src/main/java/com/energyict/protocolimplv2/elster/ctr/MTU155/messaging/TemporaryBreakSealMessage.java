package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.cpo.Environment;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.SealStatusBit;
import com.energyict.protocolimplv2.messages.SecurityMessage;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class TemporaryBreakSealMessage extends AbstractMTU155Message {

    private final SealConfig sealConfig;
    private static final int MAX_BREAK_TIME = 255;

    public TemporaryBreakSealMessage(Messaging messaging) {
        super(messaging);
        sealConfig = new SealConfig(messaging.getProtocol().getRequestFactory());
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(SecurityMessage.TEMPORARY_BREAK_SEALS.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);

        try {
            int eventLogReset = validateAndGetBreakTime(collectedMessage, message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
            int restoreFactorySettings = validateAndGetBreakTime(collectedMessage, message.getDeviceMessageAttributes().get(1).getDeviceMessageAttributeValue());
            int restoreDefaultSettings = validateAndGetBreakTime(collectedMessage, message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue());
            int statusChange = validateAndGetBreakTime(collectedMessage, message.getDeviceMessageAttributes().get(3).getDeviceMessageAttributeValue());
            int remoteConversionParamConfig = validateAndGetBreakTime(collectedMessage, message.getDeviceMessageAttributes().get(4).getDeviceMessageAttributeValue());
            int remoteAnalysisParamConfig = validateAndGetBreakTime(collectedMessage, message.getDeviceMessageAttributes().get(5).getDeviceMessageAttributeValue());
            int downloadProgram = validateAndGetBreakTime(collectedMessage, message.getDeviceMessageAttributes().get(6).getDeviceMessageAttributeValue());
            int restoreDefaultPasswords = validateAndGetBreakTime(collectedMessage, message.getDeviceMessageAttributes().get(7).getDeviceMessageAttributeValue());

            changeSealStatus(eventLogReset, SealStatusBit.EVENT_LOG_RESET);
            changeSealStatus(restoreFactorySettings, SealStatusBit.FACTORY_CONDITIONS);
            changeSealStatus(restoreDefaultSettings, SealStatusBit.DEFAULT_VALUES);
            changeSealStatus(statusChange, SealStatusBit.STATUS_CHANGE);
            changeSealStatus(remoteConversionParamConfig, SealStatusBit.REMOTE_CONFIG_VOLUME);
            changeSealStatus(remoteAnalysisParamConfig, SealStatusBit.REMOTE_CONFIG_ANALYSIS);
            changeSealStatus(downloadProgram, SealStatusBit.DOWNLOAD_PROGRAM);
            changeSealStatus(restoreDefaultPasswords, SealStatusBit.RESTORE_DEFAULT_PASSWORDS);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
    }

    private int validateAndGetBreakTime(CollectedMessage collectedMessage, String breakTimeString) throws CTRException {
        int breakTime = Integer.parseInt(breakTimeString);
        if ((breakTime < 0) && (breakTime > MAX_BREAK_TIME)) {
            String msg = "Temporary breaking a seal is only allowed for a time period between 0 and " + MAX_BREAK_TIME + ", but was [" + breakTime + "]";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
        return breakTime;
    }

    private void changeSealStatus(int breakForThisTime, SealStatusBit statusBit) throws CTRException {
        try {
            if (breakForThisTime != 0) {
                getLogger().severe("Breaking seal [" + statusBit + "] for a given time [" + breakForThisTime + "]");
                getSealConfig().breakSealTemporary(statusBit, breakForThisTime);
                addWriteDataBlockToWDBList(getFactory().getWriteDataBlockID());
            }
        } catch (CTRException e) {
            throw new CTRException("Error breaking seal [" + statusBit + "] for a given time [" + breakForThisTime + "]: " + e.getMessage());
        }
    }

    public SealConfig getSealConfig() {
        return sealConfig;
    }
}