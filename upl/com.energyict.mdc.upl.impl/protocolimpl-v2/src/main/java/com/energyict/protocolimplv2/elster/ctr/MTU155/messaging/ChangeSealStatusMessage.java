package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.SealStatusBit;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class ChangeSealStatusMessage extends AbstractMTU155Message {

    private final SealConfig sealConfig;

    public ChangeSealStatusMessage(Messaging messaging, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(messaging, collectedDataFactory, issueFactory);
        sealConfig = new SealConfig(messaging.getProtocol().getRequestFactory());
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getSpecification().getId() == SecurityMessage.BREAK_OR_RESTORE_SEALS.id();
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        Boolean eventLogReset = SecurityMessage.SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.eventLogResetSealAttributeName).getValue());
        Boolean restoreFactorySettings = SecurityMessage.SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreFactorySettingsSealAttributeName).getValue());
        Boolean restoreDefaultSettings = SecurityMessage.SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreDefaultSettingsSealAttributeName).getValue());
        Boolean statusChange = SecurityMessage.SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.statusChangeSealAttributeName).getValue());
        Boolean remoteConversionParamConfig = SecurityMessage.SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.remoteConversionParametersConfigSealAttributeName).getValue());
        Boolean remoteAnalysisParamConfig = SecurityMessage.SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeName).getValue());
        Boolean downloadProgram = SecurityMessage.SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.downloadProgramSealAttributeName).getValue());
        Boolean restoreDefaultPasswords = SecurityMessage.SealActions.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreDefaultPasswordSealAttributeName).getValue());

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
