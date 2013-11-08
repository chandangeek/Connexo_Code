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
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
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
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        Boolean eventLogReset = ProtocolTools.getBooleanFromString(getDeviceMessageAttribute(message, DeviceMessageConstants.eventLogResetSealAttributeName).getDeviceMessageAttributeValue());
        Boolean restoreFactorySettings = ProtocolTools.getBooleanFromString(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreFactorySettingsSealAttributeName).getDeviceMessageAttributeValue());
        Boolean restoreDefaultSettings = ProtocolTools.getBooleanFromString(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreDefaultSettingsSealAttributeName).getDeviceMessageAttributeValue());
        Boolean statusChange = ProtocolTools.getBooleanFromString(getDeviceMessageAttribute(message, DeviceMessageConstants.statusChangeSealAttributeName).getDeviceMessageAttributeValue());
        Boolean remoteConversionParamConfig = ProtocolTools.getBooleanFromString(getDeviceMessageAttribute(message, DeviceMessageConstants.remoteConversionParametersConfigSealAttributeName).getDeviceMessageAttributeValue());
        Boolean remoteAnalysisParamConfig =ProtocolTools.getBooleanFromString(getDeviceMessageAttribute(message, DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeName).getDeviceMessageAttributeValue());
        Boolean downloadProgram = ProtocolTools.getBooleanFromString(getDeviceMessageAttribute(message, DeviceMessageConstants.downloadProgramSealAttributeName).getDeviceMessageAttributeValue());
        Boolean restoreDefaultPasswords = ProtocolTools.getBooleanFromString(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreDefaultPasswordSealAttributeName).getDeviceMessageAttributeValue());

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
            if (activate) {
                getLogger().severe("Restoring seal " + statusBit);
                getSealConfig().restoreSeal(statusBit);
            } else {
                getLogger().severe("Breaking seal " + statusBit);
                getSealConfig().breakSealPermanent(statusBit);
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
