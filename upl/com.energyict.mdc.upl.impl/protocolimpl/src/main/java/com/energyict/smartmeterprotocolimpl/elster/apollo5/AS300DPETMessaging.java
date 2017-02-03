package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300MessageExecutor;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300Messaging;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/11/11
 * Time: 15:59
 */
public class AS300DPETMessaging extends AS300Messaging {

    public static final String GENERATE_NEW_PUBLIC_KEY = "GenerateNewPublicKey";
    public static final String SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP = "SetPublicKeysOfAggregationGroup";
    public static final String KEY = "Key";

    private static final String ID_OF_THE_METER_GROUP = "ID of the meter group";

    public AS300DPETMessaging(final AS300MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> messageCategories = super.getMessageCategories();
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
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        return super.writeTag(msgTag);
    }
}