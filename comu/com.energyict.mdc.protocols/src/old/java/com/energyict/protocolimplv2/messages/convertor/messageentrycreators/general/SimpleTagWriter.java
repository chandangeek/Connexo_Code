/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;

import java.util.Iterator;

public class SimpleTagWriter {

    public static String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
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
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
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

    public static String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }
}