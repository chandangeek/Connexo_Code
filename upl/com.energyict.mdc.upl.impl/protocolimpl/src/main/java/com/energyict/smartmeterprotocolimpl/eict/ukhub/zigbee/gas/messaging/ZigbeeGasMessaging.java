package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging;

import com.energyict.cbo.ApplicationException;
import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 16:55
 */
public class ZigbeeGasMessaging extends GenericMessaging implements TimeOfUseMessaging {

    private static final String SET_PRICE_PER_UNIT = "SetPricePerUnit";
    private static final String READ_PRICE_PER_UNIT = "ReadPricePerUnit";
    private static final String SET_STANDING_CHARGE = "SetStandingCharge";
    private static final String SET_CALORIFIC_VALUE = "SetCalorificValue";
    private static final String SET_CONVERSION_FACTOR = "SetConversionFactor";
    private static final String ID_OF_USER_FILE = "ID of user file containing the price information";
    private static final String COMMA_SEPARATED_PRICES = "CommaSeparatedPrices";
    private static final String ACTIVATION_DATE_TAG = "ActivationDate";
    private static final String ACTIVATION_DATE = "Activation date (dd/mm/yyyy hh:mm:ss) (optional)";
    private static final String STANDING_CHARGE = "Standing charge";
    private static final String CALORIFIC_VALUE = "Calorific value";
    private static final String CONVERSION_FACTOR = "Conversion factor";

    private final ZigbeeMessageExecutor messageExecutor;

    protected static final String REMOTECONNECT = "RemoteConnect";
    protected static final String REMOTEDISCONNECT = "RemoteDisconnect";

    public ZigbeeGasMessaging(final ZigbeeMessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    public void applyMessages(List messageEntries) throws IOException {
        // Nothing to do here
    }

    private TimeOfUseMessageBuilder messageBuilder = null;

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.executeMessageEntry(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec pricingInformationCategory = ProtocolMessageCategories.getPricingInformationCategory();
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Set price per unit (p/kWh)", SET_PRICE_PER_UNIT, false, ACTIVATION_DATE, ID_OF_USER_FILE));
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Set standing charge", SET_STANDING_CHARGE, false, ACTIVATION_DATE, STANDING_CHARGE));
        pricingInformationCategory.addMessageSpec(addMsgWithValues("Read price per unit costs (p/kWh)", READ_PRICE_PER_UNIT, false, true));
        categories.add(pricingInformationCategory);

        MessageCategorySpec cat2 = new MessageCategorySpec("CV & CF information");
        cat2.addMessageSpec(addMsgWithValuesAndOptionalValue("Set calorific value", SET_CALORIFIC_VALUE, false, ACTIVATION_DATE, CALORIFIC_VALUE));
        cat2.addMessageSpec(addMsgWithValuesAndOptionalValue("Set conversion factor", SET_CONVERSION_FACTOR, false, ACTIVATION_DATE, CONVERSION_FACTOR));
        categories.add(cat2);

        categories.add(ProtocolMessageCategories.getChangeOfTenancyCategory());
        categories.add(ProtocolMessageCategories.getChangeOfSupplierCategory());

        MessageCategorySpec connectDisconnectCat = new MessageCategorySpec("Connect/disconnect");
        connectDisconnectCat.addMessageSpec(addMsgWithValues("Remote connect", REMOTECONNECT, false, false));
        connectDisconnectCat.addMessageSpec(addMsgWithValues("Remote disconnect",  REMOTEDISCONNECT, false, false));
        categories.add(connectDisconnectCat);

        categories.add(getTestCategory());
        return categories;
    }

    protected MessageSpec addMsgWithValues(final String description, final String tagName, final boolean advanced, boolean required, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
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

    protected MessageSpec addMsgWithValuesAndOptionalValue(final String description, final String tagName, final boolean advanced, String lastAttribute, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        tagSpec.add(new MessageAttributeSpec(lastAttribute, false));
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        if (messageBuilder == null) {
            this.messageBuilder = new ZigbeeTimeOfUseMessageBuilder();
        }
        return messageBuilder;
    }

    public TimeOfUseMessagingConfig getTimeOfUseMessagingConfig() {
        TimeOfUseMessagingConfig config = new TimeOfUseMessagingConfig();
        config.setNeedsName(true);
        config.setSupportsCodeTables(true);
        config.setZipContent(true);
        return config;
    }

    @Override
    public String writeTag(final MessageTag msgTag) {
        if (msgTag.getName().equals(SET_PRICE_PER_UNIT)) {
            StringBuilder builder = new StringBuilder();

            // a. Opening tag
            builder.append("<");
            builder.append(msgTag.getName());
            builder.append(">");

            int userFileID = -1;
            String activationDate = "0";

            // b. Attributes
            for (Object o1 : msgTag.getAttributes()) {
                MessageAttribute att = (MessageAttribute) o1;
                if (ID_OF_USER_FILE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        try {
                            userFileID = Integer.parseInt(att.getValue());
                        } catch (NumberFormatException e) {
                            throw new ApplicationException("No user file found with ID " + userFileID);
                        }
                    }
                } else if (ACTIVATION_DATE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        activationDate = att.getValue();
                    }
                }
            }

            String commaSeparatedPrices = "";
            if (userFileID > 0) {
                MeteringWarehouse meteringWarehouse = MeteringWarehouse.getCurrent();
                UserFile userFile = meteringWarehouse.getUserFileFactory().find(userFileID);
                if (userFile != null) {
                    File file = userFile.getShadow().getFile();
                    try {
                        DataInputStream in = new DataInputStream(new FileInputStream(file));
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String strLine;
                        while ((strLine = br.readLine()) != null) {
                            commaSeparatedPrices += (strLine + ",");
                        }
                        in.close();
                    } catch (FileNotFoundException e) {
                        throw new ApplicationException(e.getMessage());
                    } catch (IOException e) {
                        throw new ApplicationException(e.getMessage());
                    }
                } else {
                    throw new ApplicationException("No user file found with ID " + userFileID);
                }
            } else {
                throw new ApplicationException("Invalid user file ID: " + userFileID);
            }
            commaSeparatedPrices = commaSeparatedPrices.substring(0, commaSeparatedPrices.lastIndexOf(","));      //Remove the last comma
            addChildTag(builder, COMMA_SEPARATED_PRICES, commaSeparatedPrices);
            addChildTag(builder, ACTIVATION_DATE_TAG, activationDate);

            // d. Closing tag
            builder.append("</");
            builder.append(msgTag.getName());
            builder.append(">");
            return builder.toString();
        } else if (msgTag.getName().equals(RtuMessageConstant.UPDATE_PRICING_INFORMATION)) {

            int userFileId = 0;
            for (Object maObject : msgTag.getAttributes()) {
                MessageAttribute ma = (MessageAttribute) maObject;
                if (ma.getSpec().getName().equals(RtuMessageConstant.UPDATE_PRICING_INFORMATION_USERFILE_ID)) {
                    if (ma.getValue() != null && ma.getValue().length() != 0) {
                        userFileId = Integer.valueOf(ma.getValue());
                    }
                }
            }

            StringBuilder builder = new StringBuilder();
            addOpeningTag(builder, msgTag.getName());
            builder.append("<").append(INCLUDED_USERFILE_TAG).append(">");

            // This will generate a message that will make the RtuMessageContentParser inline the file.
            builder.append("<").append(INCLUDE_USERFILE_TAG).append(" ").append(INCLUDE_USERFILE_ID_ATTRIBUTE).append("=\"").append(userFileId).append("\"");
            builder.append(" ").append(CREATEZIP_ATTRIBUTE_TAG).append("=\"true\"");
            builder.append("/>");

            builder.append("</").append(INCLUDED_USERFILE_TAG).append(">");
            addClosingTag(builder, msgTag.getName());
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