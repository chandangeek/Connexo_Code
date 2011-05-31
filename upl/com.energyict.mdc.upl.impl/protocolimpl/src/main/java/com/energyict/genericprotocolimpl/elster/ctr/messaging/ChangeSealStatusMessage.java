package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.info.SealStatusBit;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class ChangeSealStatusMessage extends AbstractMTU155Message {

    private static final String MESSAGE_TAG = "ChangeSealStatus";
    private static final String MESSAGE_DESCRIPTION = "Break or restore MTU seals";

    private static final String ATTR_EVENT_LOG_RESET = "EventLogReset";
    private static final String ATTR_RESTORE_FACTORY_SETTINGS = "RestoreFactorySettings";
    private static final String ATTR_RESTORE_DEFAULT_SETTINGS = "RestoreDefaultSettings";
    private static final String ATTR_STATUS_CHANGE = "StatusChange";
    private static final String ATTR_REMOTE_CONVERSION_PARAMETERS_CONFIG = "RemoteConversionParametersConfig";
    private static final String ATTR_REMOTE_ANALYSIS_PARAMETERS_CONFIG = "RemoteAnalysisParametersConfig";
    private static final String ATTR_DOWNLOAD_PROGRAM = "DownloadProgram";
    private static final String ATTR_RESTORE_DEFFAULT_PASSWORDS = "RestoreDefaultPassword";

    private final SealConfig sealConfig;

    public ChangeSealStatusMessage(MTU155MessageExecutor messageExecutor) {
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

        Boolean eventLogReset = validateNewSealStatus(eventLogResetAttr);
        Boolean restoreFactorySettings = validateNewSealStatus(restoreFactorySettingsAttr);
        Boolean restoreDefaultSettings = validateNewSealStatus(restoreDefaultSettingsAttr);
        Boolean statusChange = validateNewSealStatus(statusChangeAttr);
        Boolean remoteConversionParamConfig = validateNewSealStatus(remoteConversionParamConfigAttr);
        Boolean remoteAnalysisParamConfig = validateNewSealStatus(remoteAnalysisParamConfigAttr);
        Boolean downloadProgram = validateNewSealStatus(downloadProgramAttr);
        Boolean restoreDefaultPasswords = validateNewSealStatus(restoreDefaultPasswordsAttr);

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

    private void changeSealStatus(Boolean activate, SealStatusBit statusBit) throws CTRException {
        try {
            if (activate != null) {
                if (activate) {
                    getLogger().severe("Restoring seal "+statusBit);
                    getSealConfig().restoreSeal(statusBit);
                } else {
                    getLogger().severe("Breaking seal "+statusBit);
                    getSealConfig().breakSealPermanent(statusBit);
                }
            }
        } catch (CTRException e) {
            throw new CTRException("Error changing seal ["+statusBit+"] to ["+activate+"]: " + e.getMessage());
        }
    }

    private Boolean validateNewSealStatus(String sealAttr) throws BusinessException {
        if (sealAttr == null) {
            return null;
        }
        sealAttr = sealAttr.trim();
        if (sealAttr.equalsIgnoreCase("-")) {
            return null;
        }
        return ProtocolTools.getBooleanFromString(sealAttr);
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
