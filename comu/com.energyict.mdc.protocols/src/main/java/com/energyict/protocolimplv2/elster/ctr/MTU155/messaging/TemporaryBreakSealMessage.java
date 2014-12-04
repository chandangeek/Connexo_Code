package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.SealStatusBit;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
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
        return message.getDeviceMessageSpecPrimaryKey().equals(SecurityMessage.TEMPORARY_BREAK_SEALS.getPrimaryKey().getValue());
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        int eventLogReset = validateAndGetBreakTime(getDeviceMessageAttribute(message, DeviceMessageConstants.eventLogResetSealBreakTimeAttributeName).getDeviceMessageAttributeValue());
        int restoreFactorySettings = validateAndGetBreakTime(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreFactorySettingsSealBreakTimeAttributeName).getDeviceMessageAttributeValue());
        int restoreDefaultSettings = validateAndGetBreakTime(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreDefaultSettingsSealBreakTimeAttributeName).getDeviceMessageAttributeValue());
        int statusChange = validateAndGetBreakTime(getDeviceMessageAttribute(message, DeviceMessageConstants.statusChangeSealBreakTimeAttributeName).getDeviceMessageAttributeValue());
        int remoteConversionParamConfig = validateAndGetBreakTime(getDeviceMessageAttribute(message, DeviceMessageConstants.remoteConversionParametersConfigSealBreakTimeAttributeName).getDeviceMessageAttributeValue());
        int remoteAnalysisParamConfig = validateAndGetBreakTime(getDeviceMessageAttribute(message, DeviceMessageConstants.remoteAnalysisParametersConfigSealBreakTimeAttributeName).getDeviceMessageAttributeValue());
        int downloadProgram = validateAndGetBreakTime(getDeviceMessageAttribute(message, DeviceMessageConstants.downloadProgramSealBreakTimeAttributeName).getDeviceMessageAttributeValue());
        int restoreDefaultPasswords = validateAndGetBreakTime(getDeviceMessageAttribute(message, DeviceMessageConstants.restoreDefaultPasswordSealBreakTimeAttributeName).getDeviceMessageAttributeValue());

        changeSealStatus(eventLogReset, SealStatusBit.EVENT_LOG_RESET);
        changeSealStatus(restoreFactorySettings, SealStatusBit.FACTORY_CONDITIONS);
        changeSealStatus(restoreDefaultSettings, SealStatusBit.DEFAULT_VALUES);
        changeSealStatus(statusChange, SealStatusBit.STATUS_CHANGE);
        changeSealStatus(remoteConversionParamConfig, SealStatusBit.REMOTE_CONFIG_VOLUME);
        changeSealStatus(remoteAnalysisParamConfig, SealStatusBit.REMOTE_CONFIG_ANALYSIS);
        changeSealStatus(downloadProgram, SealStatusBit.DOWNLOAD_PROGRAM);
        changeSealStatus(restoreDefaultPasswords, SealStatusBit.RESTORE_DEFAULT_PASSWORDS);
        return null;
    }

    private int validateAndGetBreakTime(String breakTimeString) throws CTRException {
        int breakTime = Integer.parseInt(breakTimeString);
        if ((breakTime < 0) && (breakTime > MAX_BREAK_TIME)) {
            String msg = "Temporary breaking a seal is only allowed for a time period between 0 and " + MAX_BREAK_TIME + ", but was [" + breakTime + "]";
            throw new CTRException(msg);
        }
        return breakTime;
    }

    private void changeSealStatus(int breakForThisTime, SealStatusBit statusBit) throws CTRException {
        try {
            if (breakForThisTime != 0) {
                getLogger().severe("Breaking seal [" + statusBit + "] for a given time [" + breakForThisTime + "]");
                getSealConfig().breakSealTemporary(statusBit, breakForThisTime);
            }
        } catch (CTRException e) {
            throw new CTRException("Error breaking seal [" + statusBit + "] for a given time [" + breakForThisTime + "]: " + e.getMessage());
        }
    }

    public SealConfig getSealConfig() {
        return sealConfig;
    }
}