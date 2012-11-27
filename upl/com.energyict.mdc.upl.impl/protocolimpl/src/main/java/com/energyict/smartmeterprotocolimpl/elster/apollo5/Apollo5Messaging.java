package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.cbo.ApplicationException;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RtuRegisterReading;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.messaging.*;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300MessageExecutor;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300Messaging;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/11/11
 * Time: 15:59
 */
public class Apollo5Messaging extends AS300Messaging {

    private static final String GENERATE_NEW_KEY_PAIR = "GenerateNewKeyPair";
    private static final String READ_OWN_PUBLIC_KEYS = "ReadOwnPublicKeys";
    private static final String SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP = "SetPublicKeysOfAggregationGroup";
    private static final String ID_OF_THE_METER_GROUP = "ID of the meter group";
    private static final String KEY_PAIR = "KeyPair";
    private static final ObisCode PUBLIC_KEYS = ObisCode.fromString("0.128.0.2.0.2");

    public Apollo5Messaging(final AS300MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public List getMessageCategories() {
        List messageCategories = super.getMessageCategories();
        MessageCategorySpec cat1 = new MessageCategorySpec("Alliander PET");
        cat1.addMessageSpec(addMsgWithValues("Generate new keypair", GENERATE_NEW_KEY_PAIR, false, false, "Random number (optional)"));
        cat1.addMessageSpec(addMsgWithValues("Read own public key", READ_OWN_PUBLIC_KEYS, false, true));
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

        //Find RTU's in group, find the public key pairs and put them in the xml message
        if (msgTag.getName().equals(SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP)) {
            StringBuilder builder = new StringBuilder();

            // a. Opening tag
            builder.append("<");
            builder.append(msgTag.getName());
            builder.append(">");

            int groupId = -1;

            // b. Attributes
            for (Object o1 : msgTag.getAttributes()) {
                MessageAttribute att = (MessageAttribute) o1;
                if (ID_OF_THE_METER_GROUP.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        try {
                            groupId = Integer.parseInt(att.getValue());
                        } catch (NumberFormatException e) {
                            throw new ApplicationException("No group found with ID " + groupId);
                        }
                    }
                }
            }

            if (groupId > 0) {
                MeteringWarehouse meteringWarehouse = MeteringWarehouse.getCurrent();
                Group group = meteringWarehouse.getGroupFactory().find(groupId);
                if (group != null) {
                    int index = 1;
                    for (Object o : group.getMembers()) {
                        Device device = (Device) o;
                        Register register = device.getRegister(PUBLIC_KEYS);
                        if (register != null) {
                            List<RtuRegisterReading> lastXReadings = register.getLastXReadings(1);
                            if (lastXReadings.size() > 0) {
                                String keyPair = lastXReadings.get(0).getText();
                                addChildTag(builder, KEY_PAIR + String.valueOf(index), keyPair);
                                index++;
                            } else {
                                throw new ApplicationException("Device with serial number " + device.getSerialNumber() + " doesn't have a value for the public key register.");
                            }
                        } else {
                            throw new ApplicationException("Device with serial number " + device.getSerialNumber() + " doesn't have the public key register defined.");
                        }
                    }
                } else {
                    throw new ApplicationException("No group found with ID " + groupId);
                }
            } else {
                throw new ApplicationException("Invalid group ID: " + groupId);
            }

            // d. Closing tag
            builder.append("</");
            builder.append(msgTag.getName());
            builder.append(">");
            return builder.toString();
        } else {
            return super.writeTag(msgTag);
        }
    }

    /**
     * Adds a child tag to the given {@link StringBuffer}.
     *
     * @param buf     The string builder to whose contents the child tag needs to be added.
     * @param tagName The name of the child tag to add.
     * @param value   The contents (value) of the tag.
     */
    protected void addChildTag(StringBuilder buf, String tagName, Object value) {
        buf.append(System.getProperty("line.separator"));
        buf.append("<");
        buf.append(tagName);
        buf.append(">");
        buf.append(value);
        buf.append("</");
        buf.append(tagName);
        buf.append(">");
    }
}