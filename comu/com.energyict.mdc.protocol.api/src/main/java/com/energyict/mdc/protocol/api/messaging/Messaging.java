package com.energyict.mdc.protocol.api.messaging;

import java.util.List;

public interface Messaging {

    List getMessageCategories();

    String writeMessage(Message msg);

    String writeTag(MessageTag tag);

    String writeValue(MessageValue value);
}
