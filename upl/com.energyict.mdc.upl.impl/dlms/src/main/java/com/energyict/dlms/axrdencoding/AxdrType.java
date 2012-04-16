package com.energyict.dlms.axrdencoding;

/**
 * Copyrights EnergyICT
 * Date: 4/16/12
 * Time: 8:39 AM
 */
public enum AxdrType {

    NULL(0x00),
    ARRAY(0x01),
    STRUCTURE(0x02),
    BOOLEAN(0x03),
    BIT_STRING(0x04),
    DOUBLE_LONG(0x05),
    DOUBLE_LONG_UNSIGNED(0x06),
    FLOATING_POINT(0x07),
    OCTET_STRING(0x09),
    VISIBLE_STRING(0x0A),
    TIME(0x0B),
    BCD(0x0D),
    INTEGER(0x0F),
    LONG(0x10),
    UNSIGNED(0x11),
    LONG_UNSIGNED(0x12),
    COMPACT_ARRAY(0x13),
    LONG64(0x14),
    LONG64_UNSIGNED(0x15),
    ENUM(0x16),
    FLOAT32(0x17),
    FLOAT64(0x18),
    DATE_TIME(0x19),
    DATE(0x1A);

    private final byte tag;

    private AxdrType(int tag) {
        this.tag = (byte) (tag & 0x0FF);
    }

    public byte getTag() {
        return this.tag;
    }

}
