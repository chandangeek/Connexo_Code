package com.energyict.protocolimplv2.common.messaging.xmlparser;

/**
 * Created by cisac on 3/02/2017.
 */
public enum DLMSDataTypes {
    NULL(0x00, "null-data"),
    ARRAY(0x01, "array"),
    STRUCTURE(0x02, "structure"),
    BOOLEAN(0x03, "boolean"),
    BIT_STRING(0x04, "bit-string"),
    DOUBLE_LONG(0x05, "double-long"),
    DOUBLE_LONG_UNSIGNED(0x06, "double-long-unsigned"),
    OCTET_STRING(0x09, "octet-string"),
    VISIBLE_STRING(0x0A, "visible-string"),
    UTF8_STRING(0x0B, "utf8-string"),
    BCD(0x0D, "bcd"),
    INTEGER(0x0F, "integer"),
    LONG(0x10, "long"),
    UNSIGNED(0x11, "unsigned"),
    LONG_UNSIGNED(0x12, "long-unsigned"),
    COMPACT_ARRAY(0x13, "compact-array"),
    LONG64(0x14, "long64"),
    LONG64_UNSIGNED(0x15, "long64-unsigned"),
    ENUM(0x16, "enum"),
    FLOAT32(0x17, "float32"),
    FLOAT64(0x18, "float64"),
    DATE_TIME(0x19, "date-time"),
    DATE(0x1A, "date"),
    TIME(0x1B, "time"),
    INVALID(0xFF, "invalid");

    private final int tag;
    private final String description;

    DLMSDataTypes(int tag, String description){
        this.tag = tag;
        this.description = description;
    }

    public static DLMSDataTypes getFromDescription(String description){
        for(DLMSDataTypes type: values()){
            if (type.getDescription().equalsIgnoreCase(description)) {
                return type;
            }
        }
        return INVALID;
    }

    public String getDescription() {
        return description;
    }

    public int getTag() { return tag; }

}
