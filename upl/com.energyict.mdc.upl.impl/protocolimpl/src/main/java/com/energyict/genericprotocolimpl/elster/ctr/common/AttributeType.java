package com.energyict.genericprotocolimpl.elster.ctr.common;

/**
 * Copyrights EnergyICT
 * Date: 1-okt-2010
 * Time: 14:39:39
 */
public class AttributeType extends AbstractField<AttributeType> {

    private int attributeType;

    public byte[] getBytes() {
        return new byte[]{(byte) attributeType};
    }

    public AttributeType parse(byte[] rawData, int offset) throws CTRParsingException {
        return this;
    }

    public int getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(int attributeType) {
        this.attributeType = attributeType;
    }

    public boolean hasQualifier() {
        return false;
    }

    public boolean hasValueFields() {
        return false;
    }

    public boolean hasAccessDescriptor() {
        return false;
    }

    public boolean hasDefaultValue() {
        return false;
    }

}
