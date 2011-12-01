package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 5-sep-2011
 * Time: 8:38:03
 */
public class Dsmr40Messaging extends Dsmr23Messaging{

    private final Dsmr40MessageExecutor messageExecutor;

    public Dsmr40Messaging(final GenericMessageExecutor messageExecutor) {
        super(messageExecutor);
        this.messageExecutor = (Dsmr40MessageExecutor) messageExecutor;
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> messages = super.getMessageCategories();
        //TODO enable once it is required
//        messages.add(ProtocolMessageCategories.getChangeAdministrativeStatusCategory());
        return messages;
    }
}
