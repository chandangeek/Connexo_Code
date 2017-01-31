/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.iskra.mt880;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 14/10/13 - 12:07
 */
public class MT880Messaging extends ProtocolMessages {

    public static ObisCode BILLING_RESET_OBIS = ObisCode.fromString("0.0.10.0.1.255");

    private IskraMT880 protocol;

    public MT880Messaging(IskraMT880 protocol) {
        this.protocol = protocol;
    }

    public void applyMessages(List messageEntries) throws IOException {
        //nothing to do here ...
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> messageCategories = new ArrayList<MessageCategorySpec>(1);
        messageCategories.add(ProtocolMessageCategories.getDemandResetCategory());
        return messageCategories;
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if ((isItThisMessage(messageEntry, RtuMessageConstant.DEMAND_RESET))) {
                infoLog("Sending message DemandReset.");
                doBillingReset();
                infoLog("DemandReset message successful.");
                return MessageResult.createSuccess(messageEntry);
            } else {
                infoLog("Unknown message received.");
                return MessageResult.createUnknown(messageEntry);
            }
        } catch (DataAccessResultException e) {
            infoLog("Message failed : " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void doBillingReset() throws IOException {
        ScriptTable billingResetScriptTable = protocol.getDlmsSession().getCosemObjectFactory().getScriptTable(BILLING_RESET_OBIS);
        billingResetScriptTable.execute(1);
    }

    /**
     * Log the given message to the logger with the INFO level
     *
     * @param messageToLog
     */
    private void infoLog(String messageToLog) {
        this.protocol.getLogger().info(messageToLog);
    }
}
