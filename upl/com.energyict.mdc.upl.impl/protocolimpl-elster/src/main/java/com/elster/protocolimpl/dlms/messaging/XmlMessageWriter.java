package com.elster.protocolimpl.dlms.messaging;

import com.elster.protocolimpl.dlms.tariff.CodeTableBase64Builder;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.DeviceMessageFile;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights
 * Date: 9/06/11
 * Time: 11:32
 */
public class XmlMessageWriter implements Messaging {

    private final TariffCalendarFinder finder;
    private final TariffCalendarExtractor extractor;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;
    private final DeviceMessageFileFinder deviceMessageFileFinder;

    public XmlMessageWriter(TariffCalendarFinder finder, TariffCalendarExtractor extractor, DeviceMessageFileFinder deviceMessageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        this.finder = finder;
        this.extractor = extractor;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
        this.deviceMessageFileFinder = deviceMessageFileFinder;
    }

    public List<MessageCategorySpec> getMessageCategories() {
        return Collections.emptyList();
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag tag) {
        switch (tag.getName()) {
            case TariffUploadPassiveMessage.MESSAGE_TAG:
                return writeTariffTag(tag);
            case A1WriteSpecialDaysTableMessage.MESSAGE_TAG:
                return writeUserFileTag(tag, A1WriteSpecialDaysTableMessage.ATTR_SPT_FILE);
            case A1WritePassiveCalendarMessage.MESSAGE_TAG:
                return writeUserFileTag(tag, A1WritePassiveCalendarMessage.ATTR_TC_FILE);
            default:
                return writeNormalTag(tag);
        }
    }

    private String writeUserFileTag(MessageTag msgTag, String fileTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (MessageAttribute ma : msgTag.getAttributes()) {
            String specName = ma.getSpec().getName();
            if (specName.equals(fileTag)) {
                if ((ma.getValue() != null) && (!ma.getValue().isEmpty())) {
                    String[] nameParts = ma.getValue().split("\\.");
                    String name = nameParts[0];

                    List<? extends DeviceMessageFile> deviceMessageFiles = deviceMessageFileFinder.fromName(name);

                    String data = "";
                    if (!deviceMessageFiles.isEmpty()) {
                        data = new String((deviceMessageFileExtractor.binaryContents(deviceMessageFiles.get(0))));
                        data = data.replaceAll("\"", "''");
                    }
                    builder.append(" ").append(specName);
                    builder.append("=").append('"').append(data).append('"');
                }
            } else {
                if ((ma.getValue() != null) && (!ma.getValue().isEmpty())) {
                    builder.append(" ").append(specName);
                    builder.append("=").append('"').append(ma.getValue()).append('"');
                }
            }
        }
        builder.append(">");

        // c. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public String writeTariffTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        int codeTableId;
        for (MessageAttribute ma : msgTag.getAttributes()) {
            String specName = ma.getSpec().getName();
            if (specName.equals(TariffUploadPassiveMessage.ATTR_CODE_TABLE_ID)) {
                if ((ma.getValue() != null) && (!ma.getValue().isEmpty())) {
                    codeTableId = Integer.valueOf(ma.getValue());
                    String base64 = CodeTableBase64Builder.getXmlStringFromCodeTable(codeTableId, this.finder, this.extractor);
                    builder.append(" ").append(specName);
                    builder.append("=").append('"').append(base64).append('"');
                }
            } else {
                if ((ma.getValue() != null) && (!ma.getValue().isEmpty())) {
                    builder.append(" ").append(specName);
                    builder.append("=").append('"').append(ma.getValue()).append('"');
                }
            }
        }
        builder.append(">");

        // c. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    public String writeNormalTag(MessageTag msgTag) {
        StringBuilder buf = new StringBuilder();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (MessageAttribute att : msgTag.getAttributes()) {
            if (att.getValue() == null || att.getValue().isEmpty()) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Object o : msgTag.getSubElements()) {
            MessageElement elt = (MessageElement) o;
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.isEmpty()) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");

        return buf.toString();
    }

}
