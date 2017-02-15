/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.Data;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;

import static com.energyict.protocolimpl.utils.MessagingTools.getContentOfAttribute;
import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;
import static com.energyict.protocolimpl.utils.ProtocolTools.isOdd;

public class AS220IEC1107AccessController {

    public static final String ATTR_IEC_CLASS_ID = "IEC1107ClassId";
    public static final String ATTR_OFFSET = "Offset";
    public static final String ATTR_RAW_DATA = "RawData";

    private static final int MAX_OFFSET = 9999;
    private static final int MAX_CLASS_ID = 9999;
    private static final ObisCode IEC_CLASS_ACCESS_OBIS = ObisCode.fromString("0.0.96.51.0.255");

    private final AS220 as220;

    public AS220IEC1107AccessController(AS220 as220) {
        this.as220 = as220;
    }

    public void executeMessage(MessageEntry messageEntry) throws IOException {
        String classIdAttr = getContentOfAttribute(messageEntry, ATTR_IEC_CLASS_ID);
        String rawDataAttr = getContentOfAttribute(messageEntry, ATTR_RAW_DATA);
        String offsetAttr = getContentOfAttribute(messageEntry, ATTR_OFFSET);

        int classId = validateAndGetClassID(classIdAttr);
        byte[] rawData = validateAndGetRawData(rawDataAttr);
        int offset = validateAndGetOffset(offsetAttr);

        writeData(classId, offset, rawData);

    }

    private void writeData(int classId, int offset, byte[] rawData) throws IOException {
        getAs220().getLogger().severe("Writing RawData to IEC class ["+classId+"] with offset ["+offset+"]. RawData is [" + new String(rawData) + "]");
        Data data = getAs220().getCosemObjectFactory().getData(IEC_CLASS_ACCESS_OBIS);
        if (data != null) {
            Structure sequence = new Structure(
                    new Unsigned8(classId),
                    new Unsigned16(offset),
                    OctetString.fromByteArray(rawData)
            );
            data.setValueAttr(sequence);
            getAs220().getLogger().severe("Successfully send RawData to IEC class ["+classId+"] with offset ["+offset+"].");
        } else {
            throw new IOException("Unable to retrieve IEC_CLASS_ACCESS data object. Returned null!");
        }
    }

    private int validateAndGetClassID(String classIdAttr) throws IOException {
        validateNotNull(classIdAttr, ATTR_IEC_CLASS_ID);
        int classId = 0;
        try {
            classId = Integer.valueOf(classIdAttr);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid IEC class ID [" + classIdAttr + "]! " + e.getMessage());
        }
        if ((classId < 0) || (classId > MAX_CLASS_ID)) {
            throw new IOException("Invalid IEC class ID [" + classId + "]! Should be value between 0 and " + MAX_CLASS_ID);
        }
        return classId;
    }

    private byte[] validateAndGetRawData(String rawData) throws IOException {
        validateNotNull(rawData, ATTR_RAW_DATA);
        rawData = rawData.trim();
        rawData = rawData.toUpperCase();
        rawData = rawData.replaceAll("\n", "");
        rawData = rawData.replaceAll("\r", "");
        rawData = rawData.replaceAll(" ", "");
        rawData = rawData.replaceAll("\t", "");
        rawData = rawData.replaceAll("$", "");
        if (isOdd(rawData.length())) {
            throw new IOException("Invalid RawData string. Should have an even amount of HEX characters [0-9, A-F] but has [" + rawData.length() + "] characters. [" + rawData + "]");
        }
        try {
            getBytesFromHexString(rawData, "");
        } catch (Exception e) {
            throw new IOException("Unable to parse RawData: " + e.getMessage() + " [" + rawData + "]");
        }
        return rawData.getBytes();
    }

    private int validateAndGetOffset(String offsetAttr) throws IOException {
        int offset = 0;
        if (offsetAttr != null) {
            try {
                offset = Integer.valueOf(offsetAttr);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid offset [" + offsetAttr + "]! " + e.getMessage());
            }
            if ((offset < 0) || (offset > MAX_OFFSET)) {
                throw new IOException("Invalid offset [" + offset + "]! Should be value between 0 and " + MAX_OFFSET);
            }
        }
        return offset;
    }

    private void validateNotNull(String attribute, String name) throws IOException {
        if (attribute == null) {
            throw new IOException(name + " cannot be null");
        }
    }

    public AS220 getAs220() {
        return as220;
    }

    public static MessageSpec createWriteIEC1107ClassMessageSpec(String display, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(display, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_IEC_CLASS_ID, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_OFFSET, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_RAW_DATA, true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
