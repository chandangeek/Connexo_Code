package com.energyict.protocolimplv2.umi.types;

import com.energyict.protocolimplv2.umi.util.Limits;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UmiObjectPart extends LittleEndianData {
    public static final int SIZE = 9;

    public static final Pattern OBJECT_PART_REGEX =
            Pattern.compile("(\\[([0-9]{0,5})(:([0-9]{0,5}))?\\])?(\\/([0-9]{1,3}))?");

    /**
     * The UMI Code of the object to access.
     * size = 4 byte
     */
    private final UmiCode umiCode;

    /**
     * The starting element number (inclusive).
     * size = 2 bytes
     */
    private int startElement;

    /**
     * The ending element number (inclusive).
     * size = 2 bytes
     */
    private int endElement;

    /**
     * The member to write, or 0xFF to write all members.
     * The first member of the structure is 0x00, and so on.
     * size = 1 byte
     */
    private short member;

    private boolean isArray = false; // used for generating the string representation

    public UmiObjectPart(UmiCode code, int arrayElement) {
        this(code, arrayElement, arrayElement, (short) 0xFF);
    }

    public UmiObjectPart(UmiCode code, short structMember) {
        this(code, 0x0, 0x0, structMember);
    }

    public UmiObjectPart(UmiCode code, int arrayStartElement, int arrayEndElement, short structMember) {
        super(SIZE);
        this.umiCode = code;
        if (arrayStartElement > arrayEndElement) {
            throw new InvalidParameterException("arrayStartElement > arrayEndElement");
        }

        checkArrayElements(arrayStartElement, arrayEndElement);
        checkStructMember(structMember);

        startElement = arrayStartElement;
        endElement = arrayEndElement;
        member = structMember;
        getRawBuffer().put(code.getRaw()).putShort((short) startElement).putShort((short) endElement).put((byte) member);
    }

    public UmiObjectPart(byte[] raw) {
        super(raw, SIZE, false);
        byte[] umiCodeRaw = new byte[UmiCode.SIZE];
        getRawBuffer().get(umiCodeRaw);
        umiCode = new UmiCode(umiCodeRaw);

        startElement = Short.toUnsignedInt(getRawBuffer().getShort());
        endElement = Short.toUnsignedInt(getRawBuffer().getShort());
        member = (short) Byte.toUnsignedInt(getRawBuffer().get());
    }

    public UmiObjectPart(String objectPart) {
        super(SIZE);

        int index = objectPart.indexOf('[');
        if (index == -1) {
            index = objectPart.indexOf('/');
        }
        String stringUmiCode = (index != -1) ? objectPart.substring(0, index) : objectPart;

        umiCode = new UmiCode(stringUmiCode);

        if (index != -1) {
            String objectPartIndexing = objectPart.substring(index);
            parseObjectPartIndexing(objectPartIndexing);
        } else {
            startElement = 0;
            endElement = 0;
            member = 0xFF;
        }
        getRawBuffer().put(umiCode.getRaw()).putShort((short) startElement).putShort((short) endElement).put((byte) member);
    }

    private static void checkArrayElement(int arrayElement) {
        if (arrayElement < Limits.MIN_UNSIGNED || arrayElement > Limits.MAX_UNSIGNED_SHORT) {
            throw new InvalidParameterException("arrayElement must be in range=[" + Limits.MIN_UNSIGNED + ", "
                    + Limits.MAX_UNSIGNED_SHORT + "]");
        }
    }

    private static void checkStructMember(short structMember) {
        if (structMember < Limits.MIN_UNSIGNED || structMember > Limits.MAX_UNSIGNED_BYTE) {
            throw new InvalidParameterException("structMember must be in range=[" + Limits.MIN_UNSIGNED + ", "
                    + Limits.MAX_UNSIGNED_SHORT + "]");
        }
    }

    private static void checkArrayElements(int startElement, int endElement) {
        if (startElement > endElement) {
            throw new InvalidParameterException("arrayStartElement > arrayEndElement");
        }
        checkArrayElement(startElement);
        checkArrayElement(endElement);
    }

    private void parseObjectPartIndexing(String objectPartIndexing) {
        int arrayStartElement = 0;
        int arrayEndElement = 0;
        short structMember = 0;
        Matcher matcher = OBJECT_PART_REGEX.matcher(objectPartIndexing);
        if (matcher.matches()) {
            if (matcher.group(1) != null) { // [startElement:endElement]
                String startElementStr = matcher.group(2);
                arrayStartElement = startElementStr.isEmpty() ? 0 : Integer.parseInt(startElementStr);
                if (matcher.group(4) != null) { // :endElement
                    String endElementStr = matcher.group(4);
                    arrayEndElement = endElementStr.isEmpty() ? 0xFFFF : Integer.parseInt(endElementStr);
                } else { // [:] or [startElement:] or [startElement]
                    arrayEndElement = matcher.group(3) != null ? 0xFFFF : arrayStartElement;
                    if (startElementStr.isEmpty() && matcher.group(3) != null && matcher.group(5) != null) { // [:]/member
                        // cannot handle it
                        throw new InvalidParameterException("Empty start and end array indexes with struct member specified.");
                    }
                }
                if (matcher.group(5) == null) { // [startElement:endElement] without struct member specified
                    structMember = 0xFF;
                }
            }
            if (matcher.group(5) != null) { // /member
                structMember = Short.parseShort(matcher.group(6));
            }
        } else {
            throw new InvalidParameterException(
                    "Invalid UMI object part indexing, required=\"umi.byte3.byte2.byte1.byte0[start:end]/member\"");
        }
        checkArrayElements(arrayStartElement, arrayEndElement);
        checkStructMember(structMember);

        this.startElement = arrayStartElement;
        this.endElement = arrayEndElement;
        this.member = structMember;
    }

    public UmiCode getUmiCode() {
        return umiCode;
    }

    public int getStartElement() {
        return startElement;
    }

    public int getEndElement() {
        return endElement;
    }

    public short getMember() {
        return member;
    }

    public void isArray(boolean isArray) {
        this.isArray = isArray;
    }

    @Override
    public String toString() {
        String finalString = umiCode.toString();
        if (startElement == endElement) {
            if (isArray) {
                if (startElement == 0) {
                    finalString += "[0]";
                }
            } else {
                if (startElement != 0) {
                    finalString += "[" + startElement + "]";
                }
            }
        } else {
            if (startElement == 0 && endElement == 0xFFFF) {
                finalString += "[:]";
            } else if (endElement == 0xFFFF) {
                finalString += "[" + startElement + ":]";
            } else if (startElement == 0) {
                finalString += "[:" + endElement + "]";
            } else {
                finalString += "[" + startElement + ":" + endElement + "]";
            }
        }
        if (member != 0xFF) {
            finalString += "/" + member;
        }
        return finalString;
    }
}
