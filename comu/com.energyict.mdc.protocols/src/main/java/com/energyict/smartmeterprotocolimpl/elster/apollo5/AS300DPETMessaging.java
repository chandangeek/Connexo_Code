/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;

import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300MessageExecutor;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300Messaging;

import java.util.List;

public class AS300DPETMessaging extends AS300Messaging {

    public static final String GENERATE_NEW_PUBLIC_KEY = "GenerateNewPublicKey";
    public static final String SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP = "SetPublicKeysOfAggregationGroup";
    public static final String KEY = "Key";

    private static final String ID_OF_THE_METER_GROUP = "ID of the meter group";
    private static final ObisCode PUBLIC_KEYS_OBISCODE = ObisCode.fromString("0.128.0.2.0.2");

    public AS300DPETMessaging(final AS300MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public List getMessageCategories() {
        List messageCategories = super.getMessageCategories();
        MessageCategorySpec cat1 = new MessageCategorySpec("Alliander PET");
        cat1.addMessageSpec(addMsgWithValues("Generate new public key", GENERATE_NEW_PUBLIC_KEY, false, false, "Random 32 bytes (optional)"));
        cat1.addMessageSpec(addMsgWithValues("Set public keys of aggregation group", SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP, false, true, ID_OF_THE_METER_GROUP));
        messageCategories.add(cat1);
        return messageCategories;
    }

    protected MessageSpec addMsgWithValues(final String keyId, final String tagName, final boolean advanced, boolean required, String... attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, required));
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        return super.writeTag(msgTag);
    }
}