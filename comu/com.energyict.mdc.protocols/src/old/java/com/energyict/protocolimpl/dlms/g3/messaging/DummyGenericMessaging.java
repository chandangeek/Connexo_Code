/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging;

import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;

import com.energyict.protocolimpl.generic.messages.GenericMessaging;

import java.util.ArrayList;
import java.util.List;

public class DummyGenericMessaging extends GenericMessaging {

    @Override
    public List getMessageCategories() {
        return new ArrayList<MessageCategorySpec>();
    }
}
