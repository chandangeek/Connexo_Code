package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.AccessDescriptor;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.Qualifier;
import com.energyict.protocolimplv2.elster.ctr.MTU155.primitive.CTRPrimitiveParser;

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
     */
    public T parse(byte[] rawData, int ptr, AttributeType type) {
        setType(type);
        CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static

        this.setSymbol(getSymbol(getId()));

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

        return (T) this;
    }

    /**
     *
     * @param intValues
     * @return
     */
    protected static final int[] gi(int... intValues) {
        return intValues;
    }

}