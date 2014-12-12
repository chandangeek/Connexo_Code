package com.energyict.protocolimplv2.elster.ctr.MTU155.common;

import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * AttributeType indicates the fields (Id, Qlf, Val, Access or Def) in a CTR object.
 * Copyrights EnergyICT
 * Date: 1-okt-2010
 * Time: 14:39:39
 */
public class AttributeType extends AbstractField<AttributeType> {

    private static final int QUALIFIER_BIT = 0;
    private static final int VALUEFIELDS_BIT = 1;
    private static final int ACCESSDESCRIPTOR_BIT = 2;
    private static final int DEFAULTVALUE_BIT = 3;
    private static final int MASK = 0x0F;

    private boolean hasIdentifier = false;
    private boolean registerQuery = false;
    private int attributeType;

    public AttributeType() {
        this(0);
    }

    public AttributeType(int attributeType) {
        this.attributeType = attributeType & MASK;
    }

    public AttributeType(AttributeType type) {
        this.hasIdentifier = type.hasIdentifier();
        this.registerQuery = type.isRegisterQuery();
        this.attributeType = type.getAttributeType();
    }

    public static AttributeType getValueOnly() {
        AttributeType type = new AttributeType(0);
        type.setHasValueFields(true);
        type.setHasIdentifier(false);
        return type;
    }

    public static AttributeType getValueAndObjectId() {
        AttributeType type = new AttributeType(0);
        type.setHasValueFields(true);
        type.setHasIdentifier(true);
        return type;
    }

    public static AttributeType getQualifierAndValue() {
        AttributeType type = new AttributeType(0);
        type.setHasValueFields(true);
        type.setHasQualifier(true);
        type.setHasIdentifier(false);
        return type;
    }

    public int getLength() {
        return 1;
    }

    public boolean hasIdentifier() {
        return hasIdentifier;
    }

    public void setHasIdentifier(boolean hasIdentifier) {
        this.hasIdentifier = hasIdentifier;
    }

    public byte[] getBytes() {
        return getBytesFromInt(attributeType, getLength());
    }

    public AttributeType parse(byte[] rawData, int offset) throws CTRParsingException {
        attributeType = rawData[offset] & MASK;
        return this;
    }

    public int getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(int attributeType) {
        this.attributeType = attributeType & MASK;
    }

    public void setHasAccessDescriptor(boolean hasAccessDescriptor) {
        attributeType = setBit(attributeType, hasAccessDescriptor, ACCESSDESCRIPTOR_BIT);
    }

    public void setHasDefaultValue(boolean hasDefaultValue) {
        attributeType = setBit(attributeType, hasDefaultValue, DEFAULTVALUE_BIT);
    }

    public void setHasQualifier(boolean hasQualifier) {
        attributeType = setBit(attributeType, hasQualifier, QUALIFIER_BIT);
    }

    public void setHasValueFields(boolean hasValueFields) {
        attributeType = setBit(attributeType, hasValueFields, VALUEFIELDS_BIT);
    }

    public boolean hasQualifier() {
        return isBitSet(attributeType, QUALIFIER_BIT);
    }

    public boolean hasValueFields() {
        return isBitSet(attributeType, VALUEFIELDS_BIT);
    }

    public boolean hasAccessDescriptor() {
        return isBitSet(attributeType, ACCESSDESCRIPTOR_BIT);
    }

    public boolean hasDefaultValue() {
        return isBitSet(attributeType, DEFAULTVALUE_BIT);
    }

    public boolean isRegisterQuery() {
        return registerQuery;
    }

    public void setRegisterQuery(boolean registerQuery) {
        this.registerQuery = registerQuery;
    }
}
