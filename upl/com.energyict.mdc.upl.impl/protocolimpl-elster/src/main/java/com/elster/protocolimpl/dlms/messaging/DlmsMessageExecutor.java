package com.elster.protocolimpl.dlms.messaging;

import com.elster.protocolimpl.dlms.Dlms;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.MessageCategorySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights
 * Date: 7/06/11
 * Time: 13:03
 */
public class DlmsMessageExecutor {

    private final Dlms dlms;

    public DlmsMessageExecutor(Dlms dlms) {
        this.dlms = dlms;
    }

    public Logger getLogger() {
        return getDlms().getLogger();
    }

    public MessageResult doMessage(MessageEntry messageEntry) {
        boolean messageFound = false;
        boolean success = false;
        boolean fuTimeout = false;

        AbstractDlmsMessage[] messages = getSupportedMessages();
        try {
            for (AbstractDlmsMessage message : messages) {
                if (message.canExecuteThisMessage(messageEntry)) {
                    messageFound = true;
                    message.executeMessage(messageEntry);
                    success = true;
                    break;
                }
            }
        } catch (FirmwareUpdateTimeoutException fute) {
            fuTimeout = true;
        } catch (BusinessException e) {
            getLogger().severe(e.getMessage());
        }

        if (fuTimeout) {
            getLogger().warning("Message \"firmwareupdate\" will retry due to timeout.");
            return MessageResult.createQueued(messageEntry);
        }

        if (success) {
            getLogger().severe("Message " + messageEntry.toString() + " has finished successfully.");
            return MessageResult.createSuccess(messageEntry);
        }

        String msg = messageFound ? "Message " + messageEntry.toString() + " failed." : "Unknown message: " + messageEntry.toString();
        getLogger().severe(msg);

        return MessageResult.createFailed(messageEntry);

    }

    private AbstractDlmsMessage[] getSupportedMessages() {
        return new AbstractDlmsMessage[]{
                new ApnSetupMessage(this),
                new MeterLocationMessage(this),
                new TariffUploadPassiveMessage(this),
                new TariffDisablePassiveMessage(this),
                new WriteMeterMasterDataMessage(this),
                new WritePDRMessage(this),
                new ForceSyncClockMessage(this),
                new WriteGasParametersMessage(this),
                new WriteAutoConnectMessage(this),
                new DisableAutoConnectMessage(this),
                new WriteAutoAnswerMessage(this),
                new DisableAutoAnswerMessage(this),
                new ChangeKeysMessage(this),
                new FirmwareUpdateMessage(this)
        };
    }

    public Dlms getDlms() {
        return dlms;
    }

    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> messageCategories = new ArrayList<MessageCategorySpec>();
        messageCategories.add(getConnectivityCategory());
        messageCategories.add(getTariffCategory());
        messageCategories.add(getCommonParameterCategory());
        messageCategories.add(getDeviceMaintenanceCategory());
        messageCategories.add(getGasParametersCategory());
        return messageCategories;
    }

    private MessageCategorySpec getGasParametersCategory() {
        MessageCategorySpec categorySpec = new MessageCategorySpec("Change gas parameters setup");
        categorySpec.addMessageSpec(WriteGasParametersMessage.getMessageSpec(false));
        return categorySpec;
    }

    private MessageCategorySpec getConnectivityCategory() {
        MessageCategorySpec categorySpec = new MessageCategorySpec("Change connectivity setup");
        categorySpec.addMessageSpec(ApnSetupMessage.getMessageSpec("Change GPRS modem setup parameters", false));
        categorySpec.addMessageSpec(WriteAutoConnectMessage.getMessageSpec(true));
        categorySpec.addMessageSpec(DisableAutoConnectMessage.getMessageSpec(true));
        categorySpec.addMessageSpec(WriteAutoAnswerMessage.getMessageSpec(true));
        categorySpec.addMessageSpec(DisableAutoAnswerMessage.getMessageSpec(true));

        return categorySpec;
    }

    private MessageCategorySpec getDeviceMaintenanceCategory() {
        MessageCategorySpec categorySpec = new MessageCategorySpec("Device maintenance");
        categorySpec.addMessageSpec(ForceSyncClockMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(ChangeKeysMessage.getMessageSpec(true));
        return categorySpec;
    }

    private MessageCategorySpec getTariffCategory() {
        MessageCategorySpec categorySpec = new MessageCategorySpec("Tariff setup");
        categorySpec.addMessageSpec(TariffUploadPassiveMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(TariffDisablePassiveMessage.getMessageSpec(false));
        return categorySpec;
    }

    private MessageCategorySpec getCommonParameterCategory() {
        MessageCategorySpec categorySpec = new MessageCategorySpec("Change common parameter");
        categorySpec.addMessageSpec(MeterLocationMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(WriteMeterMasterDataMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(WritePDRMessage.getMessageSpec(false));
        return categorySpec;
    }

}
