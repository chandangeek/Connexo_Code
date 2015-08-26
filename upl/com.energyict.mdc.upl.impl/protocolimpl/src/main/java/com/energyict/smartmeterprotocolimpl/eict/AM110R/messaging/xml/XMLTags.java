package com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging.xml;

/**
 * @author sva
 * @since 21/12/12 - 16:08
 */

public enum XMLTags {

    SMART_SET_DLMSXML   ("SMARTsetDLMSXML", XMLTagType.PARSE_CHILD_NODES),
    SESSION             ("Session", XMLTagType.PARSE_CHILD_NODES),
    SOURCE_SCHEMES      ("SourceSchemes", XMLTagType.SKIP_NODE),
    SOURCE_APPLICATION  ("SourceApplication", XMLTagType.SKIP_NODE),
    METER               ("Meter", XMLTagType.PARSE_CHILD_NODES),
    SET_REQUEST         ("SetRequest", XMLTagType.PARSE_CHILD_NODES),
    SET_REQUEST_NORMAL  ("SetRequestNormal", XMLTagType.PARSE),
    ACTION_REQUEST      ("ActionRequest", XMLTagType.PARSE_CHILD_NODES),
    ACTION_REQUEST_NORMAL("ActionRequestNormal", XMLTagType.PARSE),

    ATTRIBUTE_DESCRIPTOR("AttributeDescriptor", XMLTagType.SUB_PART),
    METHOD_DESCRIPTOR   ("MethodDescriptor", XMLTagType.SUB_PART),
    CLASS_ID            ("ClassId", XMLTagType.SUB_PART),
    INSTANCE_ID         ("InstanceId", XMLTagType.SUB_PART),
    ATTRIBUTE_ID        ("AttributeId", XMLTagType.SUB_PART),
    METHOD_ID           ("MethodId", XMLTagType.SUB_PART),

    VALUE               ("Value", XMLTagType.SUB_PART),
    METHOD_INV_PARAMETERS("MethodInvocationParameters", XMLTagType.SUB_PART),
    BOOLEAN             ("Boolean", XMLTagType.AXDR_DATA),
    OCTET_STRING        ("OctetString", XMLTagType.AXDR_DATA),
    ENUMERATED          ("Enumerated", XMLTagType.AXDR_DATA),
    INTEGER8            ("Integer8", XMLTagType.AXDR_DATA),
    INTEGER16           ("Integer16", XMLTagType.AXDR_DATA),
    INTEGER32           ("Integer32", XMLTagType.AXDR_DATA),
    UNSIGNED8           ("Unsigned8", XMLTagType.AXDR_DATA),
    UNSIGNED16          ("Unsigned16", XMLTagType.AXDR_DATA),
    UNSIGNED32          ("Unsigned32", XMLTagType.AXDR_DATA),
    STRUCTURE           ("Structure", XMLTagType.AXDR_DATA),
    ARRAY               ("Array", XMLTagType.AXDR_DATA),

    COMMENTS             ("#comment", XMLTagType.SKIP_NODE),
    UNKNOWN             ("UnknownTag", XMLTagType.SKIP_NODE);

    private final String tagName;
    private final XMLTagType type;

    private XMLTags(String tagName, XMLTagType type) {
        this.tagName = tagName;
        this.type = type;
    }

    public String getTagName() {
        return tagName;
    }

    public XMLTagType getType() {
        return type;
    }

    /**
     * Get the corresponding {@link com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging.xml.XMLTags XMLTag} for this tag.
     *
     * @param tagName The nage of the tag
     * @return The corresponding XMLTags
     */
    public static XMLTags forTag(String tagName) {
        for (XMLTags serverTaskStatus : values()) {
            if (serverTaskStatus.getTagName().equals(tagName)) {
                return serverTaskStatus;
            }
        }

        return XMLTags.UNKNOWN; // In case of an unknown tag
    }
}