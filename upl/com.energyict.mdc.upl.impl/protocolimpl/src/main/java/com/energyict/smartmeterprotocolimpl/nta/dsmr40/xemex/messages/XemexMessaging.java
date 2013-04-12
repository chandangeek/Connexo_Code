package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.messages;

import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 26/02/13 - 10:03
 */
public class XemexMessaging extends Dsmr40Messaging {

    private final Dsmr40MessageExecutor messageExecutor;

    public XemexMessaging(final GenericMessageExecutor messageExecutor) {
        super(messageExecutor);
        this.messageExecutor = (Dsmr40MessageExecutor) messageExecutor;
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.executeMessageEntry(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList();
        MessageCategorySpec catConfiguration = getConfigurationCategory();
        MessageCategorySpec catConnectivity = getConnectivityCategory();

        categories.add(catConfiguration);
        categories.add(catConnectivity);
        return categories;
    }

    private MessageCategorySpec getConfigurationCategory() {
        MessageCategorySpec catConfigurationParameters = new MessageCategorySpec(
                RtuMessageCategoryConstants.CONFIGURATION);
        MessageSpec msgSpec = addDefaultValueMsg(
                RtuMessageKeyIdConstants.ALARMFILTER,
                RtuMessageConstant.ALARM_FILTER, false);
        catConfigurationParameters.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.RESETALARMREGISTER,
                RtuMessageConstant.RESET_ALARM_REGISTER, false);
        catConfigurationParameters.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.RESETERRORREGISTER,
                RtuMessageConstant.RESET_ERROR_REGISTER, false);
        catConfigurationParameters.addMessageSpec(msgSpec);
        msgSpec = addDefaultValueMsg("Enable DST switch",
                XemexMessageExecutor.ENABLE_DST, false);
        catConfigurationParameters.addMessageSpec(msgSpec);
        return catConfigurationParameters;
    }

    private MessageCategorySpec getConnectivityCategory() {
        MessageCategorySpec catGPRSModemSetup = new MessageCategorySpec(
                RtuMessageCategoryConstants.CHANGECONNECTIVITY);
        MessageSpec msgSpec = addChangeGPRSSetup(
                RtuMessageKeyIdConstants.GPRSMODEMSETUP,
                RtuMessageConstant.GPRS_MODEM_SETUP, false);
        catGPRSModemSetup.addMessageSpec(msgSpec);
        msgSpec = addGPRSModemCredantials(RtuMessageKeyIdConstants.GPRSCREDENTIALS,
                RtuMessageConstant.GPRS_MODEM_CREDENTIALS, false);
        catGPRSModemSetup.addMessageSpec(msgSpec);
        return catGPRSModemSetup;
    }
}
