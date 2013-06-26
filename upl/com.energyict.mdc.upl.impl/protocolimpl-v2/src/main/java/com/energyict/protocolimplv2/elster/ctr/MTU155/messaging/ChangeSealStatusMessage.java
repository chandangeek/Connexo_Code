package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.cpo.Environment;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.SealStatusBit;
import com.energyict.protocolimplv2.messages.SecurityMessage;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class ChangeSealStatusMessage extends AbstractMTU155Message {

    private final SealConfig sealConfig;

    public ChangeSealStatusMessage(Messaging messaging) {
        super(messaging);
        sealConfig = new SealConfig(messaging.getProtocol().getRequestFactory());
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(SecurityMessage.BREAK_OR_RESTORE_SEALS.getPrimaryKey().getValue());
    }

    @Override
        public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        Boolean eventLogReset = ProtocolTools.getBooleanFromString(message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        Boolean restoreFactorySettings = ProtocolTools.getBooleanFromString(message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        Boolean restoreDefaultSettings = ProtocolTools.getBooleanFromString(message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        Boolean statusChange = ProtocolTools.getBooleanFromString(message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        Boolean remoteConversionParamConfig = ProtocolTools.getBooleanFromString(message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        Boolean remoteAnalysisParamConfig =ProtocolTools.getBooleanFromString(message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        Boolean downloadProgram = ProtocolTools.getBooleanFromString(message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        Boolean restoreDefaultPasswords = ProtocolTools.getBooleanFromString(message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());

        try {
            changeAllSealStatuses(collectedMessage, eventLogReset, restoreFactorySettings, restoreDefaultSettings, statusChange, remoteConversionParamConfig, remoteAnalysisParamConfig, downloadProgram, restoreDefaultPasswords);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
    }

    private void changeAllSealStatuses(CollectedMessage collectedMessage, Boolean eventLogReset, Boolean restoreFactorySettings, Boolean restoreDefaultSettings, Boolean statusChange, Boolean remoteConversionParamConfig, Boolean remoteAnalysisParamConfig, Boolean downloadProgram, Boolean restoreDefaultPasswords) throws CTRException {
        changeSealStatus(collectedMessage, eventLogReset, SealStatusBit.EVENT_LOG_RESET);
        changeSealStatus(collectedMessage, restoreFactorySettings, SealStatusBit.FACTORY_CONDITIONS);
        changeSealStatus(collectedMessage, restoreDefaultSettings, SealStatusBit.DEFAULT_VALUES);
        changeSealStatus(collectedMessage, statusChange, SealStatusBit.STATUS_CHANGE);
        changeSealStatus(collectedMessage, remoteConversionParamConfig, SealStatusBit.REMOTE_CONFIG_VOLUME);
        changeSealStatus(collectedMessage, remoteAnalysisParamConfig, SealStatusBit.REMOTE_CONFIG_ANALYSIS);
        changeSealStatus(collectedMessage, downloadProgram, SealStatusBit.DOWNLOAD_PROGRAM);
        changeSealStatus(collectedMessage, restoreDefaultPasswords, SealStatusBit.RESTORE_DEFAULT_PASSWORDS);
    }

    private void changeSealStatus(CollectedMessage collectedMessage, Boolean activate, SealStatusBit statusBit) throws CTRException {
        try {
            if (activate != null) { //TODO: check this out
                if (activate) {
                    getLogger().severe("Restoring seal "+statusBit);
                    getSealConfig().restoreSeal(statusBit);
                    addWriteDataBlockToWDBList(getFactory().getWriteDataBlockID());
                } else {
                    getLogger().severe("Breaking seal "+statusBit);
                    getSealConfig().breakSealPermanent(statusBit);
                    addWriteDataBlockToWDBList(getFactory().getWriteDataBlockID());
                }
            }
        } catch (CTRException e) {
            String msg = "Error changing seal [" + statusBit + "] to [" + activate + "]: " + e.getMessage();
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
    }
    public SealConfig getSealConfig() {
        return sealConfig;
    }
}
