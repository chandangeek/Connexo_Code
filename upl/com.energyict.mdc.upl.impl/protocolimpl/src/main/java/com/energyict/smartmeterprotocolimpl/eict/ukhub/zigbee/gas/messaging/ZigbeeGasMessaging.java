package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging;

import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.messaging.TimeOfUseMessageBuilder;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 16:55
 */
public class ZigbeeGasMessaging extends GenericMessaging {

    private static final String SET_PRICE_PER_UNIT = "SetPricePerUnit";
    private static final String READ_PRICE_PER_UNIT = "ReadPricePerUnit";
    private static final String SET_STANDING_CHARGE = "SetStandingChargeAndActivationDate";
    private static final String SET_CALORIFIC_VALUE = "SetCalorificValueAndActivationDate";
    private static final String SET_CONVERSION_FACTOR = "SetConversionFactorAndActivationDate";
    private static final String ID_OF_USER_FILE = "ID of user file containing the price information";
    private static final String TARIFF_LABEL_TAG = "TariffLabel";
    private static final String COMMA_SEPARATED_PRICES = "CommaSeparatedPrices";
    private static final String ACTIVATION_DATE_TAG = "ActivationDate";
    private static final String ACTIVATION_DATE = "Activation date (dd/mm/yyyy hh:mm:ss) (optional)";
    private static final String STANDING_CHARGE = "Standing charge";
    private static final String CALORIFIC_VALUE = "Calorific value";
    private static final String CONVERSION_FACTOR = "Conversion factor";

    private final ZigbeeMessageExecutor messageExecutor;

    protected static final String REMOTECONNECT = "RemoteConnect";
    protected static final String REMOTEDISCONNECT = "RemoteDisconnect";
    protected static final String TEXT_TO_DISPLAY = "TextToDisplay";
    protected static final String MESSAGE = "Message";
    protected static final String DURATION = "Duration of message";

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
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndRequiredValue("Set price per unit (p/kWh)", SET_PRICE_PER_UNIT, false, ID_OF_USER_FILE, ACTIVATION_DATE));
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

        MessageCategorySpec textMessagesCat = new MessageCategorySpec("Display");
        textMessagesCat.addMessageSpec(addMsgWithValuesAndOptionalValue("Send text message to display", TEXT_TO_DISPLAY, false, ACTIVATION_DATE, MESSAGE, DURATION));
        categories.add(textMessagesCat);

        categories.add(getFirmwareCategory());
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

    // One attribute is set required - all others are optional
    protected MessageSpec addMsgWithValuesAndRequiredValue(final String description, final String tagName, final boolean advanced, String requiredAttribute, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(requiredAttribute, true));
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, false));
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeTag(final MessageTag msgTag) {
        return super.writeTag(msgTag);
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