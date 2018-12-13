package com.energyict.mdc.upl.messages.legacy;

import java.util.List;

public interface Messaging {

    List<MessageCategorySpec> getMessageCategories();

    String writeMessage(Message msg);

    String writeTag(MessageTag tag);

    String writeValue(MessageValue value);

}