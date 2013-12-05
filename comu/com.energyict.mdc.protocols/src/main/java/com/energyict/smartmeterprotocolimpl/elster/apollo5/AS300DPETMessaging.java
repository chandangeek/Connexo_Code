package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
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

    public MeteringWarehouse mw() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        if (result == null) {
            return new MeteringWarehouseFactory().getBatch(false);
        } else {
            return result;
        }
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        return super.writeTag(msgTag);
    }
}