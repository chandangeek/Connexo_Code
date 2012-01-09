package com.elster.protocolimpl.dlms.messaging;

import com.elster.protocolimpl.dlms.tariff.CodeTableBase64Builder;
import com.energyict.protocol.messaging.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights
 * Date: 9/06/11
 * Time: 11:32
 */
public class XmlMessageWriter implements Messaging {

    public List getMessageCategories() {
        return new ArrayList();
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag tag) {
        if (tag.getName().equals(TariffUploadPassiveMessage.MESSAGE_TAG)) {
            return writeTariffTag(tag);
        } else {
            return writeNormalTag(tag);
        }
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
        for (Object maObject : msgTag.getAttributes()) {
            MessageAttribute ma = (MessageAttribute) maObject;
            String specName = ma.getSpec().getName();
            if (specName.equals(TariffUploadPassiveMessage.ATTR_CODE_TABLE_ID)) {
                if ((ma.getValue() != null) && (ma.getValue().length() != 0)) {
                    codeTableId = Integer.valueOf(ma.getValue());
                    String base64 = CodeTableBase64Builder.getXmlStringFromCodeTable(codeTableId);
                    builder.append(" ").append(specName);
                    builder.append("=").append('"').append(base64).append('"');
                }
            } else {
                if ((ma.getValue() != null) && (ma.getValue().length() != 0)) {
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
        for (Object o : msgTag.getAttributes()) {
            MessageAttribute att = (MessageAttribute) o;
            if (att.getValue() == null || att.getValue().length() == 0) {
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
                if (value == null || value.length() == 0) {
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
