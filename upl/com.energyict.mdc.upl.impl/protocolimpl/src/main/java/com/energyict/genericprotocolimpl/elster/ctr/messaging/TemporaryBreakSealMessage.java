package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.info.SealStatusBit;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class TemporaryBreakSealMessage extends AbstractMTU155Message {

    private static final String MESSAGE_TAG = "TemporaryBreakSeal";
    private static final String MESSAGE_DESCRIPTION = "Temporary break MTU seals for a given time";

    private static final String ATTR_EVENT_LOG_RESET = "EventLogReset";
    private static final String ATTR_RESTORE_FACTORY_SETTINGS = "RestoreFactorySettings";
    private static final String ATTR_RESTORE_DEFAULT_SETTINGS = "RestoreDefaultSettings";
    private static final String ATTR_STATUS_CHANGE = "StatusChange";
    private static final String ATTR_REMOTE_CONVERSION_PARAMETERS_CONFIG = "RemoteConversionParametersConfig";
    private static final String ATTR_REMOTE_ANALYSIS_PARAMETERS_CONFIG = "RemoteAnalysisParametersConfig";
    private static final String ATTR_DOWNLOAD_PROGRAM = "DownloadProgram";
    private static final String ATTR_RESTORE_DEFFAULT_PASSWORDS = "RestoreDefaultPassword";
    private static final int IGNORE_VALUE = -1;

    private final SealConfig sealConfig;
    private static final int MAX_BREAK_TIME = 255;

    public TemporaryBreakSealMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
        sealConfig = new SealConfig(messageExecutor.getFactory());
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String eventLogResetAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_EVENT_LOG_RESET);
        String restoreFactorySettingsAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_RESTORE_FACTORY_SETTINGS);
        String restoreDefaultSettingsAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_RESTORE_DEFAULT_SETTINGS);
        String statusChangeAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_STATUS_CHANGE);
        String remoteConversionParamConfigAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_REMOTE_CONVERSION_PARAMETERS_CONFIG);
        String remoteAnalysisParamConfigAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_REMOTE_ANALYSIS_PARAMETERS_CONFIG);
        String downloadProgramAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_DOWNLOAD_PROGRAM);
        String restoreDefaultPasswordsAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_RESTORE_DEFFAULT_PASSWORDS);

        int eventLogReset = validateBreakTime(eventLogResetAttr);
        int restoreFactorySettings = validateBreakTime(restoreFactorySettingsAttr);
        int restoreDefaultSettings = validateBreakTime(restoreDefaultSettingsAttr);
        int statusChange = validateBreakTime(statusChangeAttr);
        int remoteConversionParamConfig = validateBreakTime(remoteConversionParamConfigAttr);
        int remoteAnalysisParamConfig = validateBreakTime(remoteAnalysisParamConfigAttr);
        int downloadProgram = validateBreakTime(downloadProgramAttr);
        int restoreDefaultPasswords = validateBreakTime(restoreDefaultPasswordsAttr);

        try {
            changeSealStatus(eventLogReset, SealStatusBit.EVENT_LOG_RESET);
            changeSealStatus(restoreFactorySettings, SealStatusBit.FACTORY_CONDITIONS);
            changeSealStatus(restoreDefaultSettings, SealStatusBit.DEFAULT_VALUES);
            changeSealStatus(statusChange, SealStatusBit.STATUS_CHANGE);
            changeSealStatus(remoteConversionParamConfig, SealStatusBit.REMOTE_CONFIG_VOLUME);
            changeSealStatus(remoteAnalysisParamConfig, SealStatusBit.REMOTE_CONFIG_ANALYSIS);
            changeSealStatus(downloadProgram, SealStatusBit.DOWNLOAD_PROGRAM);
            changeSealStatus(restoreDefaultPasswords, SealStatusBit.RESTORE_DEFAULT_PASSWORDS);
        } catch (CTRException e) {
            throw new BusinessException("Unable to change seal! " + e);
        }

    }

    private void changeSealStatus(int breakForThisTime, SealStatusBit statusBit) throws CTRException {
        try {
            if (breakForThisTime != IGNORE_VALUE) {
                getLogger().severe("Breaking seal [" + statusBit + "] for a given time [" + breakForThisTime + "]");
                getSealConfig().breakSealTemporary(statusBit, breakForThisTime);
            }
        } catch (CTRException e) {
            throw new CTRException("Error breaking seal [" + statusBit + "] for a given time [" + breakForThisTime + "]: " + e.getMessage());
        }
    }

    private int validateBreakTime(String breakTimeAttr) throws BusinessException {
        int breakTime = IGNORE_VALUE;
        if (breakTimeAttr == null) {
            return breakTime;
        }
        breakTimeAttr = breakTimeAttr.trim();
        if (breakTimeAttr.equalsIgnoreCase("-")) {
            return breakTime;
        }
        try {
            breakTime = Integer.valueOf(breakTimeAttr);
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid break time given [" + breakTimeAttr + "]" + e.getMessage());
        }
        if ((breakTime < 0) && (breakTime > MAX_BREAK_TIME)) {
            throw new BusinessException("Temporary breaking a seal is only allowed for a time period between 0 and " + MAX_BREAK_TIME + ", but was [" + breakTime + "]");
        }
        return breakTime;
    }

    public SealConfig getSealConfig() {
        return sealConfig;
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_EVENT_LOG_RESET, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_RESTORE_FACTORY_SETTINGS, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_RESTORE_DEFAULT_SETTINGS, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_STATUS_CHANGE, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_REMOTE_CONVERSION_PARAMETERS_CONFIG, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_REMOTE_ANALYSIS_PARAMETERS_CONFIG, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_DOWNLOAD_PROGRAM, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_RESTORE_DEFFAULT_PASSWORDS, false));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
