package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:00:24
 */
public abstract class AbstractUnsignedBINObject<T extends AbstractUnsignedBINObject> extends AbstractCTRObject<T> {


    /**
     * Parses a given byte array, creates a CTR Object (with value of type UnsingedBIN)
     * @param rawData: the given byte array
     * @param ptr: the start position in the byte array
     * @param type: the AttributeType object, indicating the relevant fields of the CTR Object
     * @return the CTR Object
     */    public T parse(byte[] rawData, int ptr, AttributeType type) {
        setType(type);
        CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static

        if (type.hasIdentifier()) {
            ptr += CTRObjectID.LENGTH; //Skip the sent ID bytes, ID is already set in constructor
        }

        if (type.hasQualifier()) {
            Qualifier qlf = new Qualifier(parser.parseQlf(rawData, ptr));
            this.setQlf(qlf);
            if (qlf.isInvalid() && type.isRegisterQuery()) {
                return (T) this;   //If the QLF is 0xFF (invalid), return an empty object
            }
            ptr += qlf.getLength();
        }

        if (type.hasValueFields()) {
            int[] valueLength = this.getValueLengths(getId());
            this.setValue(parser.parseUnsignedBINValue(this, rawData, ptr, valueLength));
            ptr += sum(valueLength);  //There might be multiple value fields
        }

        if (type.hasAccessDescriptor()) {
            AccessDescriptor access = new AccessDescriptor(parser.parseAccess(rawData, ptr));
            this.setAccess(access);
            ptr += access.getLength();
        }

        if (type.hasDefaultValue()) {
            this.setDefault(parser.parseDefault(getId(), this.getValue()));
        }

        this.setSymbol(getSymbol(getId()));

        return (T) this;
    }

}