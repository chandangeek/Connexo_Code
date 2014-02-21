package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.FirmwareUpdateMessaging;
import com.energyict.protocol.messaging.FirmwareUpdateMessagingConfig;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocol.messaging.TimeOfUseMessageBuilder;
import com.energyict.protocol.messaging.TimeOfUseMessaging;
import com.energyict.protocol.messaging.TimeOfUseMessagingConfig;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8-aug-2011
 * Time: 14:32:04
 */
public class AS300Messaging extends GenericMessaging implements MessageProtocol, TimeOfUseMessaging, FirmwareUpdateMessaging {

    private final AS300MessageExecutor messageExecutor;
    private static final String SET_PRICE_PER_UNIT = "SetPricePerUnit";
    private static final String READ_PRICE_PER_UNIT = "ReadPricePerUnit";
    private static final String READ_ACTIVITY_CALENDAR = "ReadActivityCalendar";
    private static final String SET_STANDING_CHARGE = "SetStandingCharge";
    private static final String ID_OF_USER_FILE = "ID of user file containing the price information";
    private static final String COMMA_SEPARATED_PRICES = "CommaSeparatedPrices";
    private static final String ACTIVATION_DATE_TAG = "ActivationDate";
    private static final String ACTIVATION_DATE = "Activation date (dd/mm/yyyy hh:mm:ss) (optional)";
    private static final String STANDING_CHARGE = "Standing charge";
    protected static final String DISCONNECT_CONTROL_RECONNECT = "DisconnectControlReconnect";
    protected static final String DISCONNECT_CONTROL_DISCONNECT = "DisconnectControlDisconnect";
    protected static final String SET_DISCONNECT_CONTROL_MODE = "SetControlMode";
    protected static final String CONTROL_MODE = "Control mode (range 0 - 6)";
    protected static final String TEXT_TO_EMETER_DISPLAY = "TextToEmeterDisplay";
    protected static final String TEXT_TO_IHD = "TextToInHomeDisplay";
    protected static final String MESSAGE = "Message";
    protected static final String DURATION = "Duration of message";

    public AS300Messaging(final AS300MessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();

        MessageCategorySpec pricingInformationCategory = ProtocolMessageCategories.getPricingInformationCategory();
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Set price per unit (p/kWh)", SET_PRICE_PER_UNIT, false, ACTIVATION_DATE, ID_OF_USER_FILE));
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Set standing charge", SET_STANDING_CHARGE, false, ACTIVATION_DATE, STANDING_CHARGE));
        pricingInformationCategory.addMessageSpec(addMsgWithValues("Read price per unit costs (p/kWh)", READ_PRICE_PER_UNIT, false, true));

        MessageCategorySpec activityCalendar = new MessageCategorySpec("Activity calendar");
        activityCalendar.addMessageSpec(addMsgWithValues("Read activity calendar", READ_ACTIVITY_CALENDAR, false, true));
        categories.add(activityCalendar);

        categories.add(pricingInformationCategory);
        categories.add(ProtocolMessageCategories.getChangeOfTenancyCategory());
        categories.add(ProtocolMessageCategories.getChangeOfSupplierCategory());

        MessageCategorySpec connectDisconnectCat = new MessageCategorySpec("Connect/disconnect");
        connectDisconnectCat.addMessageSpec(addMsgWithValues("Disconnect Control - Reconnect", DISCONNECT_CONTROL_RECONNECT, false, false));
        connectDisconnectCat.addMessageSpec(addMsgWithValues("Disconnect Control - Disconnect", DISCONNECT_CONTROL_DISCONNECT, false, false));
        connectDisconnectCat.addMessageSpec(addMsgWithValues("Disconnect Control - Write control mode", SET_DISCONNECT_CONTROL_MODE, false, true, CONTROL_MODE));
        categories.add(connectDisconnectCat);

        MessageCategorySpec textMessagesCat = new MessageCategorySpec("Display");
        textMessagesCat.addMessageSpec(addMsgWithValuesAndOptionalValue("Send text message to E-meter display", TEXT_TO_EMETER_DISPLAY, false, ACTIVATION_DATE, MESSAGE, DURATION));
        textMessagesCat.addMessageSpec(addMsgWithValuesAndOptionalValue("Send text message to IHD display", TEXT_TO_IHD, false, ACTIVATION_DATE, MESSAGE, DURATION));
        categories.add(textMessagesCat);
        categories.add(getFirmwareCategory());
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

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        //currently nothing to implement
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.executeMessageEntry(messageEntry);
    }

    /**
     * Returns the message builder capable of generating and parsing 'time of use' messages.
     *
     * @return The {@link com.energyict.protocol.messaging.MessageBuilder} capable of generating and parsing 'time of use' messages.
     */
    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        return new AS300TimeOfUseMessageBuilder();
    }

    public TimeOfUseMessagingConfig getTimeOfUseMessagingConfig() {
        TimeOfUseMessagingConfig config = new TimeOfUseMessagingConfig();
        config.setNeedsName(true);
        config.setSupportsUserFiles(true);
        config.setSupportsCodeTables(true);
        config.setZipContent(true);
        return config;
    }

    public FirmwareUpdateMessagingConfig getFirmwareUpdateMessagingConfig() {
        FirmwareUpdateMessagingConfig config = new FirmwareUpdateMessagingConfig();
        config.setSupportsUserFiles(true);
        return config;
    }

    public FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder() {
        return new AS300FirmwareUpdateMessageBuilder();
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
