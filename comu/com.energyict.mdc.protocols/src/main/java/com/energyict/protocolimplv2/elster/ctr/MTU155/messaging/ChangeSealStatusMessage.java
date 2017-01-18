package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.SealActions;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.SealStatusBit;


/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class ChangeSealStatusMessage extends AbstractMTU155Message {

    private final SealConfig sealConfig;

    public ChangeSealStatusMessage(Messaging messaging, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(messaging, issueService, collectedDataFactory);
        sealConfig = new SealConfig(messaging.getProtocol().getRequestFactory());
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(DeviceMessageId.SECURITY_BREAK_OR_RESTORE_SEALS);
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        Boolean eventLogReset = SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.eventLogResetSealAttributeName).getDeviceMessageAttributeValue());
        Boolean restoreFactorySettings = SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreFactorySettingsSealAttributeName).getDeviceMessageAttributeValue());
        Boolean restoreDefaultSettings = SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreDefaultSettingsSealAttributeName).getDeviceMessageAttributeValue());
        Boolean statusChange = SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.statusChangeSealAttributeName).getDeviceMessageAttributeValue());
        Boolean remoteConversionParamConfig = SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.remoteConversionParametersConfigSealAttributeName).getDeviceMessageAttributeValue());
        Boolean remoteAnalysisParamConfig = SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeName).getDeviceMessageAttributeValue());
        Boolean downloadProgram = SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.downloadProgramSealAttributeName).getDeviceMessageAttributeValue());
        Boolean restoreDefaultPasswords = SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreDefaultPasswordSealAttributeName).getDeviceMessageAttributeValue());

        changeAllSealStatuses(eventLogReset, restoreFactorySettings, restoreDefaultSettings, statusChange, remoteConversionParamConfig, remoteAnalysisParamConfig, downloadProgram, restoreDefaultPasswords);
        return null;
    }

    private void changeAllSealStatuses(Boolean eventLogReset, Boolean restoreFactorySettings, Boolean restoreDefaultSettings, Boolean statusChange, Boolean remoteConversionParamConfig, Boolean remoteAnalysisParamConfig, Boolean downloadProgram, Boolean restoreDefaultPasswords) throws CTRException {
        changeSealStatus(eventLogReset, SealStatusBit.EVENT_LOG_RESET);
        changeSealStatus(restoreFactorySettings, SealStatusBit.FACTORY_CONDITIONS);
        changeSealStatus(restoreDefaultSettings, SealStatusBit.DEFAULT_VALUES);
        changeSealStatus(statusChange, SealStatusBit.STATUS_CHANGE);
        changeSealStatus(remoteConversionParamConfig, SealStatusBit.REMOTE_CONFIG_VOLUME);
        changeSealStatus(remoteAnalysisParamConfig, SealStatusBit.REMOTE_CONFIG_ANALYSIS);
        changeSealStatus(downloadProgram, SealStatusBit.DOWNLOAD_PROGRAM);
        changeSealStatus(restoreDefaultPasswords, SealStatusBit.RESTORE_DEFAULT_PASSWORDS);
    }

    private void changeSealStatus(Boolean activate, SealStatusBit statusBit) throws CTRException {
        try {
            if (activate != null) {
                if (activate) {
                    getLogger().severe("Restoring seal " + statusBit);
                    getSealConfig().restoreSeal(statusBit);
                } else {
                    getLogger().severe("Breaking seal " + statusBit);
                    getSealConfig().breakSealPermanent(statusBit);
                }
            }
        } catch (CTRException e) {
            String msg = "Error changing seal [" + statusBit + "] to [" + activate + "]: " + e.getMessage();
            throw new CTRException(msg);
        }
    }
    public SealConfig getSealConfig() {
        return sealConfig;
    }
}
