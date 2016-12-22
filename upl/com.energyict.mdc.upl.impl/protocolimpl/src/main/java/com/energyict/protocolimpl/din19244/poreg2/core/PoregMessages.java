package com.energyict.protocolimpl.din19244.poreg2.core;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.request.register.DaylightAlgorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the necessary methods for a message protocol
 */
public class PoregMessages implements MessageProtocol {

    private Poreg poreg;

    public static String START_OF_DST = "StartOfDST";
    public static String END_OF_DST = "EndOfDST";
    public static String ALGORITHMS = "Algorithms";

    public PoregMessages(Poreg poreg) {
        this.poreg = poreg;
    }

    private String stripOffTag(String content) {
        return content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<DemandReset")) {
                return resetDemand(messageEntry);
            } else if (messageEntry.getContent().contains(START_OF_DST)) {
                return setDSTTime(messageEntry, true);
            }   else if (messageEntry.getContent().contains(END_OF_DST)) {
                return setDSTTime(messageEntry, false);
            } else if (messageEntry.getContent().contains(ALGORITHMS)) {
                return setDSTAlgorithms(messageEntry);
            }
            return MessageResult.createFailed(messageEntry);
        }
        catch (Exception e) {
            poreg.getLogger().severe("Error parsing message, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult resetDemand(MessageEntry messageEntry) throws IOException {
        poreg.getLogger().info("Demand reset");
        poreg.getRequestFactory().resetDemand();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setDSTTime(MessageEntry messageEntry, boolean startOfDST) throws IOException {
        int month, dayOfMonth, dayOfWeek;

        String monthString = getValueFromXMLAttribute("Month", messageEntry.getContent());
        month = Integer.parseInt(monthString);
        if (month < 1 || month > 12) {
            throw new IOException("Failed to parse the message content. " + month + " is not a valid month. Message will fail.");
        }

        String dayOfMonthString = getValueFromXMLAttribute("Day of month", messageEntry.getContent());
        dayOfMonth = Integer.parseInt(dayOfMonthString);
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IOException("Failed to parse the message content. " + dayOfMonth + " is not a valid day of month. Message will fail.");
        }

        String dayOfWeekString = getValueFromXMLAttribute("Day of week", messageEntry.getContent());
        dayOfWeek = Integer.parseInt(dayOfWeekString);
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IOException("Failed to parse the message content. " + dayOfWeek + " is not a valid day of week. Message will fail.");
        }

        if (startOfDST) {
            poreg.getRegisterFactory().writeDstStart(month, dayOfMonth, dayOfWeek);
        } else {
            poreg.getRegisterFactory().writeDstEnd(month, dayOfMonth, dayOfWeek);
        }

        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setDSTAlgorithms(MessageEntry messageEntry) throws IOException {
        String startString = getValueFromXMLAttribute("Start Algorithm", messageEntry.getContent());
        DaylightAlgorithm startAlgorithm = DaylightAlgorithm.valueFromOrdinal(Integer.parseInt(startString));

        String endString = getValueFromXMLAttribute("End Algorithm", messageEntry.getContent());
        DaylightAlgorithm endAlgorithm = DaylightAlgorithm.valueFromOrdinal(Integer.parseInt(endString));

        poreg.getRegisterFactory().writeDstAlgorithms(startAlgorithm, endAlgorithm);
        return MessageResult.createSuccess(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();

        MessageCategorySpec catPoreg = new MessageCategorySpec("Poreg 2/2P messages");
        catPoreg.addMessageSpec(addBasicMsg("Demand reset", "DemandReset", false));

        theCategories.add(catPoreg);
        MessageCategorySpec catDaylightSaving = new MessageCategorySpec("Daylight saving");
        catDaylightSaving.addMessageSpec(addMsgWithValues("Program Start of Daylight Saving Time", START_OF_DST, false, true, "Month", "Day of month", "Day of week"));
        catDaylightSaving.addMessageSpec(addMsgWithValues("Program End of Daylight Saving Time", END_OF_DST, false, true, "Month", "Day of month", "Day of week"));
        catDaylightSaving.addMessageSpec(addMsgWithValues("Program Daylight Algorithms", ALGORITHMS, false, true, "Start Algorithm", "End Algorithm"));
        theCategories.add(catDaylightSaving);

        return theCategories;
    }

    private MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addMsgWithValues(final String description, final String tagName, final boolean advanced, boolean required, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, required));
        }
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private String getValueFromXMLAttribute(String tag, String content) {
        int startIndex = content.indexOf(tag + "=\"");
        if (startIndex != -1) {
            int endIndex = content.indexOf("\"", startIndex + tag.length() + 2);
            try {
                return content.substring(startIndex + tag.length() + 2, endIndex);
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return "";
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (MessageAttribute att : msgTag.getAttributes()) {
            if (att.getValue() == null || att.getValue().isEmpty()) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Object o : msgTag.getSubElements()) {
            MessageElement elt = (MessageElement) o;
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.isEmpty()) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public void applyMessages(List messageEntries) throws IOException {
    }
}