package com.energyict.protocolimpl.dlms.g3.messaging;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.messaging.MessageCategorySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Dummy class to access the methods in GenericMessaging to create common categories
 * <p/>
 * Copyrights EnergyICT
 * Date: 20/08/12
 * Time: 17:16
 * Author: khe
 */
public class DummyGenericMessaging extends GenericMessaging {

    @Override
    public List getMessageCategories() {
        return new ArrayList<MessageCategorySpec>();
    }
}
